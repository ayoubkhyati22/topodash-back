package com.topographe.topographe.mapper;

import com.topographe.topographe.dto.UserDTO;
import com.topographe.topographe.dto.referentiel.CityDto;
import com.topographe.topographe.entity.User;
import com.topographe.topographe.mapper.referentiel.CityMapper;

public class UserMapper {
    public static UserDTO toDTO(User user) {
        if (user == null) return null;

        CityDto cityDto = null;
        if (user.getCity() != null) {
            cityDto = CityMapper.toDto(user.getCity());
        }

        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getFirstName(),
                user.getLastName(),
                user.getBirthday(),
                user.getCin(),
                cityDto,
                user.getRole()
        );
    }

    public static User toEntity(UserDTO userDTO) {
        if (userDTO == null) return null;

        User user = new User();
        user.setId(userDTO.getId());
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setBirthday(userDTO.getBirthday());
        user.setCin(userDTO.getCin());
        user.setRole(userDTO.getRole());

        if (userDTO.getCityDto() != null) {
            user.setCity(CityMapper.toEntity(userDTO.getCityDto()));
        }

        return user;
    }
}