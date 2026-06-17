package Controllers;

import DAO.*;
import Helpers.ValidadorCookie;
import Model.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TesteIntegracaoLanchoneteTest {

    @Mock private DaoCliente daoClienteMock;
    @Mock private DaoToken daoTokenMock;
    @Mock private DaoFuncionario daoFuncionarioMock;
    @Mock private DaoLanche daoLancheMock;
    @Mock private DaoBebida daoBebidaMock;
    @Mock private DaoPedido daoPedidoMock;
    @Mock private ValidadorCookie validadorCookieMock;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    private StringWriter respostaHttp;
    @Before
    public void configurar() throws Exception {
        respostaHttp = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(respostaHttp));
    }

    private ServletInputStream toStream(String json) {
        ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes());
        return new ServletInputStream() {
            @Override public int read() throws IOException { return bais.read(); }
            @Override public boolean isFinished() { return bais.available() == 0; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener l) { /* not required for test stream */ }
        };
    }

    // TI-01: Login bem-sucedido deve acionar DaoCliente E DaoToken
    // Integração: login ↔ DaoCliente ↔ DaoToken
    @Test
    public void TI01_loginBemSucedidoDeveAcionarDaoClienteEDaoToken() throws Exception {
        when(request.getInputStream())
                .thenReturn(toStream("{\"usuario\":\"joao\",\"senha\":\"123\"}"));
        when(daoClienteMock.login(any(Cliente.class))).thenReturn(true);
        when(daoClienteMock.pesquisaPorUsuario(any(Cliente.class))).thenReturn(new Cliente());

        new login() {
            @Override protected DaoCliente criarDaoCliente() { return daoClienteMock; }
            @Override protected DaoToken criarDaoToken() { return daoTokenMock; }
        }.processRequest(request, response);

        verify(daoClienteMock, times(1)).login(any(Cliente.class));
        verify(daoTokenMock, times(1)).salvar(anyString());
    }

    // TI-02: Login com credenciais inválidas NÃO deve chamar DaoToken
    // Integração: login ↔ DaoCliente (falha) — DaoToken não deve ser acionado
    @Test
    public void TI02_loginFalhadoNaoDeveAcionarDaoToken() throws Exception {
        when(request.getInputStream())
                .thenReturn(toStream("{\"usuario\":\"joao\",\"senha\":\"errada\"}"));
        when(daoClienteMock.login(any(Cliente.class))).thenReturn(false);

        new login() {
            @Override protected DaoCliente criarDaoCliente() { return daoClienteMock; }
            @Override protected DaoToken criarDaoToken() { return daoTokenMock; }
        }.processRequest(request, response);

        verify(daoClienteMock, times(1)).login(any(Cliente.class));
        verify(daoTokenMock, never()).salvar(anyString());
    }

    // TI-03: Login de funcionário bem-sucedido deve acionar DaoFuncionario E DaoToken
    // Integração: loginFuncionario ↔ DaoFuncionario ↔ DaoToken
    @Test
    public void TI03_loginFuncionarioBemSucedidoDeveAcionarDaoFuncionarioEDaoToken() throws Exception {
        when(request.getInputStream())
                .thenReturn(toStream("{\"usuario\":\"admin\",\"senha\":\"admin123\"}"));
        when(daoFuncionarioMock.login(any(Funcionario.class))).thenReturn(true);
        when(daoFuncionarioMock.pesquisaPorUsuario(any(Funcionario.class))).thenReturn(new Funcionario());

        new loginFuncionario() {
            @Override protected DaoFuncionario criarDaoFuncionario() { return daoFuncionarioMock; }
            @Override protected DaoToken criarDaoToken() { return daoTokenMock; }
        }.processRequest(request, response);

        verify(daoFuncionarioMock, times(1)).login(any(Funcionario.class));
        verify(daoFuncionarioMock, times(1)).pesquisaPorUsuario(any(Funcionario.class));
        verify(daoTokenMock, times(1)).salvar(anyString());
    }

    // TI-04: Login de funcionário falhado NÃO deve chamar DaoToken
    // Integração: loginFuncionario ↔ DaoFuncionario (falha) — DaoToken isolado
    @Test
    public void TI04_loginFuncionarioFalhadoNaoDeveAcionarDaoToken() throws Exception {
        when(request.getInputStream())
                .thenReturn(toStream("{\"usuario\":\"admin\",\"senha\":\"errada\"}"));
        when(daoFuncionarioMock.login(any(Funcionario.class))).thenReturn(false);

        new loginFuncionario() {
            @Override protected DaoFuncionario criarDaoFuncionario() { return daoFuncionarioMock; }
            @Override protected DaoToken criarDaoToken() { return daoTokenMock; }
        }.processRequest(request, response);

        verify(daoFuncionarioMock, times(1)).login(any(Funcionario.class));
        verify(daoTokenMock, never()).salvar(anyString());
    }

    // TI-05: Compra com cookie válido deve acionar DaoPedido.salvar()
    // Integração: comprar ↔ ValidadorCookie ↔ DaoCliente ↔ DaoPedido
    @Test
    public void TI05_compraComCookieValidoDeveAcionarDaoPedidoSalvar() throws Exception {
        Cookie[] cookies = { new Cookie("token", "1-ok") };
        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(toStream("{\"id\":1}"));
        when(validadorCookieMock.validar(cookies)).thenReturn(true);
        when(daoClienteMock.pesquisaPorID("1")).thenReturn(new Cliente());
        when(daoPedidoMock.pesquisaPorData(any(Pedido.class))).thenReturn(new Pedido());

        new comprar() {
            @Override protected ValidadorCookie criarValidadorCookie() { return validadorCookieMock; }
            @Override protected DaoCliente criarDaoCliente() { return daoClienteMock; }
            @Override protected DaoLanche criarDaoLanche() { return daoLancheMock; }
            @Override protected DaoBebida criarDaoBebida() { return daoBebidaMock; }
            @Override protected DaoPedido criarDaoPedido() { return daoPedidoMock; }
        }.processRequest(request, response);

        verify(daoPedidoMock, times(1)).salvar(any(Pedido.class));
        assertTrue(respostaHttp.toString().contains("Pedido Salvo"));
    }

    // TI-06: Compra com cookie inválido NÃO deve acionar DaoCliente nem DaoPedido
    // Integração: comprar ↔ ValidadorCookie (barreira) — DAOs protegidos
    @Test
    public void TI06_compraComCookieInvalidoNaoDeveAcionarDaoClienteNemDaoPedido() throws Exception {
        Cookie[] cookies = { new Cookie("token", "invalido") };
        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(toStream(""));
        when(validadorCookieMock.validar(cookies)).thenReturn(false);

        new comprar() {
            @Override protected ValidadorCookie criarValidadorCookie() { return validadorCookieMock; }
            @Override protected DaoCliente criarDaoCliente() { return daoClienteMock; }
            @Override protected DaoLanche criarDaoLanche() { return daoLancheMock; }
            @Override protected DaoBebida criarDaoBebida() { return daoBebidaMock; }
            @Override protected DaoPedido criarDaoPedido() { return daoPedidoMock; }
        }.processRequest(request, response);

        verify(daoClienteMock, never()).pesquisaPorID(anyString());
        verify(daoPedidoMock, never()).salvar(any(Pedido.class));
        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // TI-07: Compra com lanche deve acionar DaoLanche e vincular ao pedido
    // Integração: comprar ↔ DaoLanche ↔ DaoPedido (vincularLanche)
    @Test
    public void TI07_compraComLancheDeveVincularLancheAoPedido() throws Exception {
        Cookie[] cookies = { new Cookie("token", "1-ok") };
        String json = "{\"id\":1,\"X-Burger\":[\"X-Burger\",\"lanche\",2]}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(toStream(json));
        when(validadorCookieMock.validar(cookies)).thenReturn(true);
        when(daoClienteMock.pesquisaPorID("1")).thenReturn(new Cliente());

        Lanche lanche = new Lanche();
        lanche.setValor_venda(18.0);
        when(daoLancheMock.pesquisaPorNome("X-Burger")).thenReturn(lanche);

        Pedido pedidoSalvo = new Pedido();
        when(daoPedidoMock.pesquisaPorData(any(Pedido.class))).thenReturn(pedidoSalvo);

        new comprar() {
            @Override protected ValidadorCookie criarValidadorCookie() { return validadorCookieMock; }
            @Override protected DaoCliente criarDaoCliente() { return daoClienteMock; }
            @Override protected DaoLanche criarDaoLanche() { return daoLancheMock; }
            @Override protected DaoBebida criarDaoBebida() { return daoBebidaMock; }
            @Override protected DaoPedido criarDaoPedido() { return daoPedidoMock; }
        }.processRequest(request, response);

        verify(daoLancheMock, times(1)).pesquisaPorNome("X-Burger");
        verify(daoPedidoMock, times(1)).vincularLanche(eq(pedidoSalvo), any(Lanche.class));
    }

    // TI-08: Compra com bebida deve acionar DaoBebida e vincular ao pedido
    // Integração: comprar ↔ DaoBebida ↔ DaoPedido (vincularBebida)
    @Test
    public void TI08_compraComBebidaDeveVincularBebidaAoPedido() throws Exception {
        Cookie[] cookies = { new Cookie("token", "1-ok") };
        String json = "{\"id\":1,\"Coca-Cola\":[\"Coca-Cola\",\"bebida\",1]}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(toStream(json));
        when(validadorCookieMock.validar(cookies)).thenReturn(true);
        when(daoClienteMock.pesquisaPorID("1")).thenReturn(new Cliente());

        Bebida bebida = new Bebida();
        bebida.setValor_venda(6.0);
        when(daoBebidaMock.pesquisaPorNome("Coca-Cola")).thenReturn(bebida);

        Pedido pedidoSalvo = new Pedido();
        when(daoPedidoMock.pesquisaPorData(any(Pedido.class))).thenReturn(pedidoSalvo);

        new comprar() {
            @Override protected ValidadorCookie criarValidadorCookie() { return validadorCookieMock; }
            @Override protected DaoCliente criarDaoCliente() { return daoClienteMock; }
            @Override protected DaoLanche criarDaoLanche() { return daoLancheMock; }
            @Override protected DaoBebida criarDaoBebida() { return daoBebidaMock; }
            @Override protected DaoPedido criarDaoPedido() { return daoPedidoMock; }
        }.processRequest(request, response);

        verify(daoBebidaMock, times(1)).pesquisaPorNome("Coca-Cola");
        verify(daoPedidoMock, times(1)).vincularBebida(eq(pedidoSalvo), any(Bebida.class));
    }

    // TI-09: Compra completa (lanche + bebida) deve vincular ambos ao pedido
    // Integração: comprar ↔ DaoLanche ↔ DaoBebida ↔ DaoPedido
    @Test
    public void TI09_compraCompletaDeveVincularLancheEBebidaAoPedido() throws Exception {
        Cookie[] cookies = { new Cookie("token", "1-ok") };
        String json = "{"
                + "\"id\":1,"
                + "\"X-Burger\":[\"X-Burger\",\"lanche\",1],"
                + "\"Suco\":[\"Suco\",\"bebida\",1]"
                + "}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(toStream(json));
        when(validadorCookieMock.validar(cookies)).thenReturn(true);
        when(daoClienteMock.pesquisaPorID("1")).thenReturn(new Cliente());

        Lanche lanche = new Lanche();
        lanche.setValor_venda(18.0);
        when(daoLancheMock.pesquisaPorNome("X-Burger")).thenReturn(lanche);

        Bebida bebida = new Bebida();
        bebida.setValor_venda(7.0);
        when(daoBebidaMock.pesquisaPorNome("Suco")).thenReturn(bebida);

        Pedido pedidoSalvo = new Pedido();
        when(daoPedidoMock.pesquisaPorData(any(Pedido.class))).thenReturn(pedidoSalvo);

        new comprar() {
            @Override protected ValidadorCookie criarValidadorCookie() { return validadorCookieMock; }
            @Override protected DaoCliente criarDaoCliente() { return daoClienteMock; }
            @Override protected DaoLanche criarDaoLanche() { return daoLancheMock; }
            @Override protected DaoBebida criarDaoBebida() { return daoBebidaMock; }
            @Override protected DaoPedido criarDaoPedido() { return daoPedidoMock; }
        }.processRequest(request, response);

        verify(daoPedidoMock, times(1)).salvar(any(Pedido.class));
        verify(daoPedidoMock, times(1)).vincularLanche(eq(pedidoSalvo), any(Lanche.class));
        verify(daoPedidoMock, times(1)).vincularBebida(eq(pedidoSalvo), any(Bebida.class));
        assertTrue(respostaHttp.toString().contains("Pedido Salvo"));
    }

    // TI-10: Cadastro deve montar Cliente com Endereço e acionar DaoCliente.salvar()
    // Integração: cadastro ↔ DaoCliente (dados compostos: Cliente + Endereco)
    @Test
    public void TI10_cadastroDevePassarClienteComEnderecoParaDaoClienteSalvar() throws Exception {
        String json = "{"
                + "\"usuario\":{"
                + "\"nome\":\"Maria\","
                + "\"sobrenome\":\"Silva\","
                + "\"telefone\":\"21988887777\","
                + "\"usuario\":\"maria\","
                + "\"senha\":\"abc123\""
                + "},"
                + "\"endereco\":{"
                + "\"bairro\":\"Icarai\","
                + "\"cidade\":\"Niteroi\","
                + "\"estado\":\"RJ\","
                + "\"complemento\":\"Casa\","
                + "\"rua\":\"Rua das Flores\","
                + "\"numero\":42"
                + "}"
                + "}";

        when(request.getInputStream()).thenReturn(toStream(json));

        cadastro servlet = new cadastro() {
            @Override protected DaoCliente criarDaoCliente() { return daoClienteMock; }
        };
        servlet.doPost(request, response);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(daoClienteMock, times(1)).salvar(captor.capture());

        Cliente clienteCapturado = captor.getValue();
        assertEquals("Maria", clienteCapturado.getNome());
        assertEquals("Silva", clienteCapturado.getSobrenome());
        assertEquals("21988887777", clienteCapturado.getTelefone());
        assertEquals("maria", clienteCapturado.getUsuario());
        assertNotNull("Endereco nao deve ser nulo", clienteCapturado.getEndereco());
        assertEquals("Icarai", clienteCapturado.getEndereco().getBairro());
        assertEquals("Niteroi", clienteCapturado.getEndereco().getCidade());
        assertEquals(42, clienteCapturado.getEndereco().getNumero());
    }
}
