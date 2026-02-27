package com.example.boardservice.service;

import com.example.boardservice.client.PointClient;
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
    private final PointClient pointClient;

    public BoardService(
            BoardRepository boardRepository,
            UserClient userClient,
            PointClient pointClient){
        this.boardRepository = boardRepository;
        this.userClient = userClient;
        this.pointClient = pointClient;
    }

    @Transactional
    public void create(CreateBoardRequestDto createBoardRequestDto){
        //게시글 작성전 100포인트 차감
        pointClient.deductPoints(createBoardRequestDto.getUserId(), 100);

        Board board = new Board(
                createBoardRequestDto.getTitle(),
                createBoardRequestDto.getContent(),
                createBoardRequestDto.getUserId()
        );

        this.boardRepository.save(board);

        //게시글 작성시 작성자에게 활동점수 10부여
        userClient.addActivityScore(createBoardRequestDto.getUserId(), 10);
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
