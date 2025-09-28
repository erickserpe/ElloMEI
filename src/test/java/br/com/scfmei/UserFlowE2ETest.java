package br.com.scfmei;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// We use a random port to avoid conflicts during tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserFlowE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        // WebDriverManager automatically downloads and configures the ChromeDriver
        WebDriverManager.chromedriver().setup();

        // Configure Chrome to run in "headless" mode (without a visible UI window)
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Max wait time of 10 seconds
    }

    @Test
    void deveRegistrarNovoUsuarioELogarComSucesso() {
        // --- ETAPA 1: REGISTO ---

        // Navega para a página de registo
        driver.get("http://localhost:" + port + "/registro");
        assertEquals("Create your Ello MEI Account", driver.getTitle());

        // Gera um nome de utilizador único para cada execução do teste
        String uniqueUsername = "e2e_user_" + UUID.randomUUID().toString().substring(0, 8);

        // Preenche o formulário de registo
        driver.findElement(By.id("username")).sendKeys(uniqueUsername);
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("nomeCompleto")).sendKeys("E2E Test User");
        driver.findElement(By.id("nomeFantasia")).sendKeys("E2E Test Company");

        // Clica no botão para criar a conta
        driver.findElement(By.className("create-account-btn")).click();

        // --- ETAPA 2: LOGIN ---

        // Espera até que a página de login seja carregada e verifica o título
        wait.until(ExpectedConditions.titleIs("Login - Ello MEI"));

        // Espera até que o campo de utilizador esteja presente e visível
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        wait.until(ExpectedConditions.elementToBeClickable(usernameField));

        // Preenche o formulário de login com as novas credenciais
        usernameField.clear();
        usernameField.sendKeys(uniqueUsername);

        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys("password123");

        // Clica no botão de login
        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.className("glass-btn")));
        loginButton.click();

        // --- ETAPA 3: VERIFICAÇÃO ---

        // Espera até que seja redirecionado para o dashboard (mais flexível)
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/dashboard"),
            ExpectedConditions.titleContains("Dashboard")
        ));

        // Verifica se o URL é o do dashboard
        assertTrue(driver.getCurrentUrl().endsWith("/dashboard"),
                "Não foi redirecionado para a página do dashboard. URL atual: " + driver.getCurrentUrl());

        System.out.println("✅ Teste E2E concluído com sucesso!");
        System.out.println("   - Utilizador registrado: " + uniqueUsername);
        System.out.println("   - Login realizado com sucesso");
        System.out.println("   - Redirecionamento para dashboard confirmado");
    }

    @AfterEach
    void tearDown() {
        // Fecha o navegador após cada teste para limpar os recursos
        if (driver != null) {
            driver.quit();
        }
    }
}
