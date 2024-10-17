package br.com.fiap.gitdash.github;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;

import java.util.List;

@Controller
public class GitHubController {

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/")
    public String getUserInfo(Model model, @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient,@AuthenticationPrincipal OAuth2User user) {

        String tokenValue = authorizedClient.getAccessToken().getTokenValue();
        List<RepositoryInfo> repos = gitHubService.getUserRepositories(tokenValue);

        var avatar = user.getAttributes().get("avatar_url");
        var nome = user.getAttributes().get("name");
        var html_url = user.getAttributes().get("html_url");

        model.addAttribute("repos", repos);

        model.addAttribute("name", nome);
        model.addAttribute("avatar_url", avatar);
        model.addAttribute("html_url", html_url);

        return "user";
    }
}

