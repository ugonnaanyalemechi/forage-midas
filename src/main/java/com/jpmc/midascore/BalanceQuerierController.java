package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class BalanceQuerierController {

    UserRepository userRepository;

    public BalanceQuerierController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/balance")
    public Balance queryBalance(@RequestParam(value = "userId") long userId) {
        Optional<UserRecord> optionalSender = Optional.ofNullable(userRepository.findById(userId));
        UserRecord user = optionalSender.orElse(null);

        if (user != null)
            return new Balance(user.getBalance());
        return new Balance(0);
    }
}
