package com.profileglance.api.service;

import com.profileglance.api.request.LookatmePostReq;
import com.profileglance.api.response.LookatmePostRes;
import com.profileglance.db.entity.Category;
import com.profileglance.db.entity.Lookatme;
import com.profileglance.db.entity.User;
import com.profileglance.db.repository.CategoryRepository;
import com.profileglance.db.repository.LookatmeRepository;
import com.profileglance.db.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LookatmeServiceImpl implements LookatmeService{

    @Autowired
    LookatmeRepository lookatmeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public List<LookatmePostRes> searchByCategory(String category, Long limit) {

        System.out.println("categoryQ : " + category);
        System.out.println("limit : " + limit);

        List<Lookatme> lookatmeList = lookatmeRepository.findAllByCategory_CategoryName(category, limit);

        List<LookatmePostRes> lookatmePostResList = new ArrayList<>();

        for (Lookatme l : lookatmeList){
            lookatmePostResList.add(new LookatmePostRes(
                    l.getLookatmeId(),
                    l.getUser().getUserNickname(),
                    l.getTitle(),
                    l.getContent(),
                    l.getVideo(),
                    l.getThumbnail(),
                    l.getCategory().getCategoryName(),
                    l.getView(),
                    l.getVideoLike(),
                    l.getCreatedAt()
            ));
        }

        return lookatmePostResList;
    }

    @Override
    public Boolean uploadLookatme(LookatmePostReq lookatmePostReq) {

        System.out.println("서비스 등록 들어왔어요~~~");
        User user = userRepository.findByUserEmail(lookatmePostReq.getUserEmail()).get();

        String categoryName = lookatmePostReq.getCategory();

        String baseDir = "C:\\Users\\multicampus\\Documents\\ServerFiles";
        String videoPath = baseDir + "\\Video\\" + lookatmePostReq.getTitle() + ".mp4";
        String thumbnailPath = baseDir + "\\Thumbnail\\" + lookatmePostReq.getTitle() + ".jpg";


        try{
            lookatmePostReq.getVideo().transferTo(new File(videoPath));
            lookatmePostReq.getThumbnail().transferTo(new File(thumbnailPath));
        } catch (Exception e){
            return false;
        }

        Category category = categoryRepository.findByCategoryName(categoryName).get();

        Lookatme lookatme = lookatmeRepository.save(Lookatme.builder().title(lookatmePostReq.getTitle())
                .content(lookatmePostReq.getContent()).thumbnail(thumbnailPath).video(videoPath).view(0L)
                .user(user).category(category).build());

        user.getLookatmes().add(lookatme);

        userRepository.save(user);

        return true;
    }

    @Override
    public Boolean updateLookatme(LookatmePostReq lookatmePostReq) {

        System.out.println("룩앳미 업데이트 서비스 입니다.");

        String baseDir = "C:\\Users\\multicampus\\Documents\\ServerFiles";
        String videoPath = baseDir + "\\Video\\" + lookatmePostReq.getTitle() + ".mp4";
        String thumbnailPath = baseDir + "\\Thumbnail\\" + lookatmePostReq.getTitle() + ".jpg";

        try{
            lookatmePostReq.getVideo().transferTo(new File(videoPath));
            lookatmePostReq.getThumbnail().transferTo(new File(thumbnailPath));
        } catch (Exception e){
            return false;
        }

        Lookatme lookatme = lookatmeRepository.findById(lookatmePostReq.getLookatmeId()).get();

        lookatme.setTitle(lookatmePostReq.getTitle());
        lookatme.setContent(lookatmePostReq.getContent());
        lookatme.setVideo(videoPath);
        lookatme.setThumbnail(thumbnailPath);
        lookatme.setCategory(categoryRepository.findByCategoryName(lookatmePostReq.getCategory()).get());

        lookatmeRepository.save(lookatme);

        return true;
    }

    @Override
    public List<LookatmePostRes> searchByTitle(String title, Long limit) {

        String titleQ = "%" + title + "%";

        List<Lookatme> lookatmeList = lookatmeRepository.findAllByTitle(titleQ, limit);

        System.out.println(lookatmeList.size());

        List<LookatmePostRes> lookatmePostResList = new ArrayList<>();

        for (Lookatme l : lookatmeList){
            lookatmePostResList.add(new LookatmePostRes(
                    l.getLookatmeId(),
                    l.getUser().getUserNickname(),
                    l.getTitle(),
                    l.getContent(),
                    l.getVideo(),
                    l.getThumbnail(),
                    l.getCategory().getCategoryName(),
                    l.getView(),
                    l.getVideoLike(),
                    l.getCreatedAt()
            ));
        }

        return lookatmePostResList;
    }

    @Override
    public List<LookatmePostRes> searchByNickname(String nickname, Long limit) {

        String nicnameQ = "%" + nickname + "%";

        List<Lookatme> lookatmeList = lookatmeRepository.findAllByUser_UserNickname(nicnameQ, limit);

        System.out.println(lookatmeList.size());

        List<LookatmePostRes> lookatmePostResList = new ArrayList<>();

        for (Lookatme l : lookatmeList){
            lookatmePostResList.add(new LookatmePostRes(
                    l.getLookatmeId(),
                    l.getUser().getUserNickname(),
                    l.getTitle(),
                    l.getContent(),
                    l.getVideo(),
                    l.getThumbnail(),
                    l.getCategory().getCategoryName(),
                    l.getView(),
                    l.getVideoLike(),
                    l.getCreatedAt()
            ));
        }

        return lookatmePostResList;
    }

    @Override
    public LookatmePostRes detailLookatme(Long lookatmeId) {
        Lookatme lookatme = lookatmeRepository.findByLookatmeId(lookatmeId).get();

        Long viewCount = lookatme.getView() + 1;
        lookatme.setView(viewCount);
        lookatmeRepository.save(lookatme);

        LookatmePostRes lookatmePostRes = new LookatmePostRes(
                lookatme.getLookatmeId(),
                lookatme.getUser().getUserNickname(),
                lookatme.getTitle(),
                lookatme.getContent(),
                lookatme.getVideo(),
                lookatme.getThumbnail(),
                lookatme.getCategory().getCategoryName(),
                lookatme.getView(),
                lookatme.getVideoLike(),
                lookatme.getCreatedAt()
        );

        return lookatmePostRes;
    }

}
