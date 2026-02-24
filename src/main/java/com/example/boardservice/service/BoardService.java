package com.example.boardservice.service;

import com.example.boardservice.client.UserClient;
import com.example.boardservice.domain.Board;
import com.example.boardservice.domain.BoardRepository;
import com.example.boardservice.dto.BoardResponseDto;
import com.example.boardservice.dto.CreateBoardRequestDto;
import com.example.boardservice.dto.UserDto;
import com.example.boardservice.dto.UserResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Optional;

@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final UserClient userClient;

    public BoardService(BoardRepository boardRepository, UserClient userClient){
        this.boardRepository = boardRepository;
        this.userClient = userClient;
    }

    @Transactional
    public void create(CreateBoardRequestDto createBoardRequestDto){
        Board board = new Board(
                createBoardRequestDto.getTitle(),
                createBoardRequestDto.getContent(),
                createBoardRequestDto.getUserId()
        );

        this.boardRepository.save(board);
    }

    public BoardResponseDto getBoard(Long boardId){
        //게시글 불러오기
        Board board = boardRepository.findById(boardId)
                .orElseThrow(()-> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        //user-service로부터 사용자 정보 불러오기
        Optional<UserResponseDto> optionalUserResponseDto = userClient.fetchUser(board.getUserId());

        //응답값 조합하기
        UserDto userDto = null;
        if(optionalUserResponseDto.isPresent()){
            UserResponseDto userResponseDto = optionalUserResponseDto.get();
            userDto = new UserDto(
                    userResponseDto.getUserId(),
                    userResponseDto.getName()
            );
        }


        BoardResponseDto boardResponseDto = new BoardResponseDto(
                board.getBoardId()
                , board.getTitile()
                , board.getContent()
                , userDto
        );

        return boardResponseDto;
    }

    //게시글 전체 조회
    public List<BoardResponseDto> getBoards(){
        List<Board> boards = boardRepository.findAll();

        //userId 목록 추출
        List<Long> userIds = boards.stream()
                .map(Board :: getUserId)
                .distinct()
                .toList();

        List<UserResponseDto> userResponseDtos = userClient.fetchUsersByIds(userIds);

        //userId를 key로하는 map을 생성
        Map<Long, UserDto> userMap = new HashMap<>();
        for (UserResponseDto userResponseDto : userResponseDtos){
            Long userId = userResponseDto.getUserId();
            String name = userResponseDto.getName();
            userMap.put(userId, new UserDto(userId, name));
        }

        return boards.stream()
                .map(board -> new BoardResponseDto(
                        board.getBoardId(),
                        board.getTitile(),
                        board.getContent(),
                        userMap.get(board.getUserId())
                )).toList();
    }
}
