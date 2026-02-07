package com.zbs.learning.service;
import com.zbs.learning.domain.User;
import com.zbs.learning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserCached(Long id) {
        User u1 = userRepository.findById(id).orElse(null);
        User u2 = userRepository.findById(id).orElse(null);
        return u2;
    }
}
