package com.cropdeal.auth.config;

import com.cropdeal.auth.entity.AuthUser;
import com.cropdeal.auth.entity.Role;
import com.cropdeal.auth.repository.AuthUserRepository;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FacebookDemoAccountInitializer implements ApplicationRunner {

	private record DemoAccount(String email, Role role) {
	}

	private static final List<DemoAccount> DEMO_ACCOUNTS = List.of(
			new DemoAccount("farmerfb@cropdeal.demo", Role.FARMER),
			new DemoAccount("dealerfb@cropdeal.demo", Role.DEALER));

	private final AuthUserRepository authUserRepository;
	private final PasswordEncoder passwordEncoder;

	public FacebookDemoAccountInitializer(AuthUserRepository authUserRepository, PasswordEncoder passwordEncoder) {
		this.authUserRepository = authUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		for (DemoAccount demo : DEMO_ACCOUNTS) {
			authUserRepository.findByEmail(demo.email()).ifPresentOrElse(user -> repairRoleIfNeeded(user, demo.role()),
					() -> createUser(demo));
		}
	}

	private void repairRoleIfNeeded(AuthUser user, Role expectedRole) {
		boolean changed = false;

		if (user.getRole() == null) {
			user.setRole(expectedRole);
			changed = true;
		}

		if (!user.isActive()) {
			user.setActive(true);
			changed = true;
		}

		if (changed) {
			authUserRepository.save(user);
		}
	}

	private void createUser(DemoAccount demo) {
		AuthUser user = new AuthUser();
		user.setEmail(demo.email());
		user.setPassword(passwordEncoder.encode("dummy-facebook-account"));
		user.setRole(demo.role());
		user.setActive(true);
		user.setCreatedAt(LocalDateTime.now());
		authUserRepository.save(user);
	}
}
