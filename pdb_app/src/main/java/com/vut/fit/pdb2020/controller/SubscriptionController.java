package com.vut.fit.pdb2020.controller;

import com.vut.fit.pdb2020.database.cassandra.domain.*;
import com.vut.fit.pdb2020.database.cassandra.repository.*;
import com.vut.fit.pdb2020.database.dto.NameProfileTuple;
import com.vut.fit.pdb2020.database.dto.SubscribeDto;
import com.vut.fit.pdb2020.database.mariaDB.domain.PageSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.SubscribtionSql;
import com.vut.fit.pdb2020.database.mariaDB.domain.UserSql;
import com.vut.fit.pdb2020.database.mariaDB.repository.PageSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.SubscriptionSqlRepository;
import com.vut.fit.pdb2020.database.mariaDB.repository.UserSqlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SubscriptionController {

    @Autowired
    UserSqlRepository userSqlRepository;

    @Autowired
    PageSqlRepository pageSqlRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SubscriptionSqlRepository subscriptionSqlRepository;

    @Autowired
    FollowerRepository followerRepository;

    @Autowired
    FollowsRepository followsRepository;

    @Autowired
    FollowsPageRepository followsPageRepository;

    @Autowired
    ProfileDictionaryRepository profileDictionaryRepository;

    @Autowired
    PageRepository pageRepository;


    @Transactional
    @PostMapping("/subscribe/user")
    public String subscribeToUser(@RequestParam String email, @RequestParam String subscribes) {

        assert email != null && subscribes != null;

        UserSql userSql = userSqlRepository.findByEmail(email);
        assert userSql != null;
        UserSql subscribesTo = userSqlRepository.findByEmail(subscribes);
        assert subscribesTo != null;

        SubscribtionSql subscribtionSql = new SubscribtionSql();
        subscribtionSql.setSubscriber(userSql);
        subscribtionSql.setSubscribedToUser(subscribesTo);
        subscribtionSql.setCreated_at(Instant.now());
        subscribtionSql.setUpdated_at(Instant.now());
        subscriptionSqlRepository.save(subscribtionSql);

        FollowsCql follows = new FollowsCql();
        follows.setUser_email(email);
        follows.setFollows_email(subscribes);
        follows.setCreated_at(Instant.now());
        followsRepository.save(follows);

        FollowerCql follower = new FollowerCql();
        follower.setFollower_email(subscribes);
        follower.setUser_email(email);
        follower.setCreated_at(Instant.now());
        followerRepository.save(follower);

        return String.format("User %s is now subscribed to user %s", email, subscribes);

    }

    @Transactional
    @PostMapping("/subscribe/page")
    public String subscribeToPage(@RequestParam String email, @RequestParam Long subscribes) {

        assert email != null && subscribes != null;

        UserSql userSql = userSqlRepository.findByEmail(email);
        assert userSql != null;
        PageSql subscribesTo = pageSqlRepository.findById(subscribes);
        assert subscribesTo != null;

        SubscribtionSql subscribtionSql = new SubscribtionSql();
        subscribtionSql.setSubscriber(userSql);
        subscribtionSql.setSubscribedToPage(subscribesTo);
        subscriptionSqlRepository.save(subscribtionSql);

        FollowsPageCql follows = new FollowsPageCql();
        follows.setUser_email(email);
        follows.setFollowsId(subscribesTo.getId());
        follows.setCreated_at(Instant.now());
        followsPageRepository.save(follows);

        FollowerCql follower = new FollowerCql();
        follower.setFollower_id(subscribesTo.getId());
        follower.setUser_email(email);
        follower.setCreated_at(Instant.now());
        followerRepository.save(follower);

        return String.format("User %s is now subscribed to page %s", email, subscribes);

    }

    @GetMapping("/user/subscribers/{slug}")
    public SubscribeDto getSubscribers(@PathVariable String slug) {

        String profilePath = "/profile/".concat(slug);

        ProfileDictionaryCql profileDictionaryCql = profileDictionaryRepository.findByPath(profilePath);

        String email = profileDictionaryCql.getUser_email();

        List<NameProfileTuple> subscribers = new ArrayList<>();

        List<FollowsCql> follows = followsRepository.findAllByFollowsEmail(email);

        for (FollowsCql follow : follows) {
            UserCql user = userRepository.findByEmail(follow.getUser_email());

            if (user != null) {
                subscribers.add(new NameProfileTuple(String.format("%s %s", user.getName(), user.getSurname()), user.getProfile_path()));
            }
            else {
                UserSql userSql = userSqlRepository.findByEmail(follow.getUser_email());
                assert userSql != null;
                subscribers.add(new NameProfileTuple(String.format("%s %s", userSql.getName(), userSql.getSurname()), userSql.getProfilePath()));
            }

        }

        SubscribeDto subscribeDto = new SubscribeDto();
        UserCql userCql = userRepository.findByEmail(email);
        if (userCql != null) {
            subscribeDto.setTarget(new NameProfileTuple(String.format("%s %s", userCql.getName(), userCql.getSurname()), userCql.getProfile_path()));
        }
        else {
            UserSql userSql = userSqlRepository.findByEmail(email);
            subscribeDto.setTarget(new NameProfileTuple(String.format("%s %s", userSql.getName(), userSql.getSurname()), userSql.getProfilePath()));
        }

        subscribeDto.setSubscriptions(subscribers);
        return subscribeDto;
    }

    @GetMapping("/page/subscribers/{slug}")
    public SubscribeDto getPageSubscribers(@PathVariable String slug) {

        String profilePath = "/page/".concat(slug);

        ProfileDictionaryCql profileDictionaryCql = profileDictionaryRepository.findByPath(profilePath);
        Long pageId = profileDictionaryCql.getPage_id();

        List<FollowsPageCql> follows = followsPageRepository.findAllByFollowsId(pageId);
        List<NameProfileTuple> subscribers = new ArrayList<>();

        for (FollowsPageCql follow : follows) {
            UserCql user = userRepository.findByEmail(follow.getUser_email());

            if (user != null) {
                subscribers.add(new NameProfileTuple(String.format("%s %s", user.getName(), user.getSurname()), user.getProfile_path()));
            }
            else {
                UserSql userSql = userSqlRepository.findByEmail(follow.getUser_email());
                assert userSql != null;
                subscribers.add(new NameProfileTuple(String.format("%s %s", userSql.getName(), userSql.getSurname()), userSql.getProfilePath()));
            }
        }

        SubscribeDto subscribeDto = new SubscribeDto();
        PageCql pageCql = pageRepository.findById(profileDictionaryCql.getPage_id());
        if (pageCql != null) {
            subscribeDto.setTarget(new NameProfileTuple(pageCql.getName(), pageCql.getProfile_path()));
        }
        else {
            PageSql pageSql = pageSqlRepository.findById(pageId);
            subscribeDto.setTarget(new NameProfileTuple(pageSql.getName(), pageSql.getProfilePath()));
        }

        subscribeDto.setSubscriptions(subscribers);
        return subscribeDto;
    }

    @GetMapping("/user/subscribed-to/{slug}")
    public SubscribeDto getSubscribedTo(@PathVariable String slug) {

        String profilePath = "/profile/".concat(slug);

        ProfileDictionaryCql profileDictionaryCql = profileDictionaryRepository.findByPath(profilePath);
        String email = profileDictionaryCql.getUser_email();

        List<FollowerCql> follows = followerRepository.findAllByUserEmail(email);

        List<NameProfileTuple> subscribers = new ArrayList<>();

        for (FollowerCql follow : follows) {
            if (follow.getFollower_email() != null) {
                UserCql follower = userRepository.findByEmail(follow.getFollower_email());

                if (follower != null) {
                    NameProfileTuple tuple = new NameProfileTuple(String.format("%s %s", follower.getName(), follower.getSurname()), follower.getProfile_path());
                    subscribers.add(tuple);
                }
                else {
                    UserSql followerSql = userSqlRepository.findByEmail(follow.getFollower_email());
                    assert followerSql != null;
                    NameProfileTuple tuple = new NameProfileTuple(String.format("%s %s", followerSql.getName(), followerSql.getSurname()), followerSql.getProfilePath());
                    subscribers.add(tuple);
                }

            }
            if (follow.getFollower_id() != null) {
                PageCql page = pageRepository.findById(follow.getFollower_id());

                if (page != null) {
                    subscribers.add(new NameProfileTuple(page.getName(), page.getProfile_path()));
                }
                else {
                    PageSql pageSql = pageSqlRepository.findById(follow.getFollower_id());
                    assert pageSql != null;
                    subscribers.add(new NameProfileTuple(pageSql.getName(), pageSql.getProfilePath()));
                }

            }

        }

        SubscribeDto subscribeDto = new SubscribeDto();
        UserCql userCql = userRepository.findByEmail(email);
        if (userCql != null) {
            subscribeDto.setTarget(new NameProfileTuple(String.format("%s %s", userCql.getName(), userCql.getSurname()), userCql.getProfile_path()));
        }
        else {
            UserSql userSql = userSqlRepository.findByEmail(email);
            subscribeDto.setTarget(new NameProfileTuple(String.format("%s %s", userSql.getName(), userSql.getSurname()), userSql.getProfilePath()));
        }
        subscribeDto.setSubscriptions(subscribers);
        return subscribeDto;
    }

    @Transactional
    @PostMapping("/unsubscribe/page")
    public String unsubscribePage(@RequestParam Long pageId, @RequestParam String email) {

        PageSql pageSql = pageSqlRepository.findById(pageId);
        assert pageSql != null;
        UserSql userSql = userSqlRepository.findByEmail(email);
        assert userSql != null;

        SubscribtionSql subscribtionSql = subscriptionSqlRepository.findBySubscriberAndSubscribedToPage(userSql, pageSql);

        subscriptionSqlRepository.delete(subscribtionSql);
        followsPageRepository.deleteByFollowsIdAndUserEmail(pageId, email);

        List<FollowerCql> followersCql = followerRepository.findAllByUserEmail(email);

        for (FollowerCql followerCql : followersCql) {
            if (followerCql.getFollower_id().equals(pageId))
                followerRepository.deleteByUserEmailAndCreatedAt(followerCql.getUser_email(), followerCql.getCreated_at());
        }

        return String.format("Succesfully unsubscribed page %s.", pageSql.getName());

    }

    @Transactional
    @PostMapping("/unsubscribe/user")
    public String unsubscribeUser(@RequestParam String userEmail, @RequestParam String unsubscribeFromEmail) {

        assert userEmail != null && unsubscribeFromEmail != null;

        UserSql userSql = userSqlRepository.findByEmail(userEmail);
        assert userSql != null;
        UserSql unsubscribeFrom = userSqlRepository.findByEmail(unsubscribeFromEmail);
        assert unsubscribeFrom != null;

        SubscribtionSql subscribtionSql = subscriptionSqlRepository.findBySubscriberAndSubscribedToUser(userSql, unsubscribeFrom);

        subscriptionSqlRepository.delete(subscribtionSql);

        followsRepository.deleteByFollowsEmailAndUserEmail(unsubscribeFromEmail, userEmail);

        List<FollowerCql> followersCql = followerRepository.findAllByUserEmail(userEmail);

        for (FollowerCql followerCql : followersCql) {
            if (followerCql.getFollower_email() != null && followerCql.getFollower_email().equals(unsubscribeFromEmail))
                followerRepository.deleteByUserEmailAndCreatedAt(followerCql.getUser_email(), followerCql.getCreated_at());
        }

        return String.format("Succesfully unsubscribed page %s.", unsubscribeFromEmail);

    }

}
