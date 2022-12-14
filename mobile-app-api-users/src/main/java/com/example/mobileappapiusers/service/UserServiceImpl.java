package com.example.mobileappapiusers.service;

import com.example.mobileappapiusers.data.UserEntity;
import com.example.mobileappapiusers.model.AlbumResponseModel;
import com.example.mobileappapiusers.model.AlbumsServiceClient;
import com.example.mobileappapiusers.model.UserResponseModel;
import com.example.mobileappapiusers.model.UserRest;
import com.example.mobileappapiusers.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {


    BCryptPasswordEncoder bCryptPasswordEncoder;

    UserRepository userRepository;
    AlbumsServiceClient albumsServiceClient;

    @Autowired
    public UserServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder, UserRepository userRepository, AlbumsServiceClient albumsServiceClient) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.albumsServiceClient = albumsServiceClient;
        this.userRepository = userRepository;
    }

    @Override
    public UserRest createUser(UserRest userDto) {
        userDto.setUserId(UUID.randomUUID().toString());
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPassword(bCryptPasswordEncoder.encode(userDto.getPassword()));
        userRepository.save(userEntity);

        return modelMapper.map(userEntity, UserRest.class);


    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByEmail(username);
        if (userEntity == null) {
            throw new UsernameNotFoundException(username);
        }
        return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), new ArrayList<>());
    }

    @Override
    public UserRest getUserDetailsByEmail(String email) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) {
            throw new UsernameNotFoundException(email);
        }
        return new ModelMapper().map(userEntity, UserRest.class);
    }

    @Override
    public UserResponseModel getUserByUserId(String userId) {
        UserEntity userEntity = userRepository.findByUserId(userId);
        if (userEntity == null) {
            throw new RuntimeException();
        }
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        UserResponseModel responseModel = modelMapper.map(userEntity, UserResponseModel.class);
        List<AlbumResponseModel> list = albumsServiceClient.getAlbums(userId); // albums/users/{id}/albums
        if(!list.isEmpty()){
            System.out.println("xiyupl");
        }
        responseModel.setAlbums(list);

        return responseModel;
    }


}
