package br.com.scfmei;

import br.com.scfmei.domain.Conta;
import br.com.scfmei.domain.Usuario;
import br.com.scfmei.repository.ContaRepository;
import br.com.scfmei.repository.LancamentoRepository;
import br.com.scfmei.repository.UsuarioRepository;
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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class LancamentoE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private LancamentoRepository lancamentoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private WebDriver driver;
    private WebDriverWait wait;
    private Usuario testUser;

    @BeforeEach
    void setUp() {
        // Configura o WebDriver
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        // Cria um utilizador e uma conta no banco de dados para o teste
        testUser = new Usuario();
        testUser.setUsername("lancamento_user");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setRoles("USER");
        testUser.setNomeCompleto("Usuario Teste E2E");
        testUser.setNomeFantasia("Empresa Teste E2E");
        // Não definimos CPF e CNPJ para evitar validações
        usuarioRepository.save(testUser);

        Conta conta = new Conta();
        conta.setNomeConta("Conta E2E");
        conta.setTipo("Teste");
        conta.setSaldoInicial(BigDecimal.ZERO);
        conta.setSaldoAtual(BigDecimal.ZERO);
        conta.setUsuario(testUser);
        contaRepository.save(conta);
    }

    @Test
    void deveCriarNovoLancamentoDeEntradaPelaUI() throws InterruptedException {
        // --- ETAPA 1: LOGIN ---
        driver.get("http://localhost:" + port + "/login");
        driver.findElement(By.id("username")).sendKeys(testUser.getUsername());
        driver.findElement(By.id("password")).sendKeys("password");
        driver.findElement(By.tagName("button")).click();
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        // --- ETAPA 2: NAVEGAÇÃO E CRIAÇÃO DO LANÇAMENTO ---
        // Navega para a página de lançamentos
        driver.findElement(By.xpath("//a[@href='/lancamentos']")).click();
        wait.until(ExpectedConditions.urlContains("/lancamentos"));

        // Abre o dropdown usando JavaScript para garantir que funcione
        WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("dropdownMenuLancamentos")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", dropdownButton);

        // Aguarda o dropdown abrir e o link "Nova Entrada" ficar visível
        WebElement novaEntradaLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(text(), 'Nova Entrada')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", novaEntradaLink);

        wait.until(ExpectedConditions.urlContains("/lancamentos/novo/entrada"));

        // Preenche o formulário de nova entrada
        String descricao = "Serviço de consultoria E2E";
        String valor = "1250.75";
        driver.findElement(By.id("descricao")).sendKeys(descricao);
        driver.findElement(By.id("data")).sendKeys(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        new Select(driver.findElement(By.id("status"))).selectByValue("PAGO");
        new Select(driver.findElement(By.xpath("//select[contains(@name, 'conta')]"))).selectByIndex(1); // Seleciona a primeira conta
        driver.findElement(By.xpath("//input[contains(@name, 'valor')]")).sendKeys(valor);

        // Submete o formulário
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        // --- ETAPA 3: VERIFICAÇÃO ---
        wait.until(ExpectedConditions.urlContains("/lancamentos"));

        // Verifica se a tabela na página de lançamentos agora contém o novo lançamento
        WebElement tabela = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
        String pageSource = driver.getPageSource();

        assertTrue(pageSource.contains(descricao), "A descrição do novo lançamento não foi encontrada na tabela.");
        // Formata o valor para o padrão brasileiro para corresponder ao que é exibido na tela
        String valorFormatado = "1.250,75";
        assertTrue(pageSource.contains(valorFormatado), "O valor do novo lançamento não foi encontrado ou não está formatado corretamente na tabela.");
        
        System.out.println("✅ Teste E2E de criação de lançamento concluído com sucesso!");
        System.out.println("   - Login realizado com sucesso");
        System.out.println("   - Navegação para página de lançamentos");
        System.out.println("   - Formulário de nova entrada preenchido");
        System.out.println("   - Lançamento criado e visível na tabela");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }

        // Limpa os dados de teste
        lancamentoRepository.deleteAll();
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }
}
