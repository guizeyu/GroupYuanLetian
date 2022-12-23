package com.advancedweb2022groupylt.finalpj.service.logic.middleStage;

import com.advancedweb2022groupylt.finalpj.bean.entity.User;
import com.advancedweb2022groupylt.finalpj.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SceneRecordService
{
    @Resource
    private UserRepository userRepository;

    public void record(String username,int idxBeforeSessionClose)
    {
        if (idxBeforeSessionClose<=0) return;
        User user = userRepository.findUserByUsername(username);
        if (user!=null)
        {
            user.setScene(idxBeforeSessionClose);
            userRepository.save(user);
        }
    }
}
