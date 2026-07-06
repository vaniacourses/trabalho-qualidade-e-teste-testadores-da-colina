package Controllers;

import DAO.DaoLanche;
import Helpers.ValidadorCookie;
import Model.Lanche;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.atLeastOnce;

// Permite usar Mockito nos testes
@RunWith(MockitoJUnitRunner.class)
public class GetLanchesTest {

    // Mocks das dependências usadas pelo servlet
    @Mock
    private ValidadorCookie validadorMock;

    @Mock
    private DaoLanche daoLancheMock;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    // Guarda a resposta escrita pelo servlet
    private StringWriter respostaHttp;

    // Classe testável para substituir dependências reais por mocks
    private class GetLanchesTestavel extends getLanches {
        @Override
        protected ValidadorCookie criarValidadorCookie() {
            return validadorMock;
        }

        @Override
        protected DaoLanche criarDaoLanche() {
            return daoLancheMock;
        }
    }

    // Executa antes de cada teste
    @Before
    public void configurar() throws Exception {
        respostaHttp = new StringWriter();

        // Faz o response escrever dentro da variável respostaHttp
        when(response.getWriter()).thenReturn(new PrintWriter(respostaHttp));
    }

    // Testa se cookie válido chama listarTodos()
    @Test
    public void cookieValidoDeveChamarListarTodos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoLancheMock.listarTodos()).thenReturn(Arrays.asList());

        new GetLanchesTestavel().processRequest(request, response);

        verify(daoLancheMock, times(1)).listarTodos();
    }

    // Testa se cookie inválido não chama listarTodos()
    @Test
    public void cookieInvalidoNaoDeveChamarListarTodos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetLanchesTestavel().processRequest(request, response);

        verify(daoLancheMock, never()).listarTodos();
    }

    // Testa se cookie inválido retorna erro
    @Test
    public void cookieInvalidoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetLanchesTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se cookie válido não retorna erro
    @Test
    public void cookieValidoNaoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoLancheMock.listarTodos()).thenReturn(Arrays.asList());

        new GetLanchesTestavel().processRequest(request, response);

        assertFalse(respostaHttp.toString().contains("erro"));
    }

    // Testa se o JSON retorna o nome do lanche
    @Test
    public void cookieValidoDeveRetornarNomeDoLanche() throws Exception {
        Lanche lanche = new Lanche();
        lanche.setNome("X-Burguer");

        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoLancheMock.listarTodos()).thenReturn(Arrays.asList(lanche));

        new GetLanchesTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("X-Burguer"));
    }

    // Testa se o doPost chama o processRequest
    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetLanchesTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se o doGet chama o processRequest
    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetLanchesTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa a descrição do servlet
    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new GetLanchesTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }

    // Testa se o response foi configurado como JSON UTF-8
    @Test
    public void deveConfigurarResponseQuandoProcessaRequisicao() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoLancheMock.listarTodos()).thenReturn(Arrays.asList());

        new GetLanchesTestavel().processRequest(request, response);

        verify(response, atLeastOnce()).setContentType("application/json");
        verify(response, atLeastOnce()).setCharacterEncoding("UTF-8");
    }

    // Testa o caso sem cookie, cobrindo o tratamento de erro
    @Test
    public void semCookieDeveRetornarErro() throws Exception {
        when(request.getCookies()).thenReturn(null);

        new GetLanchesTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
        verify(daoLancheMock, never()).listarTodos();
    }

    @Test
    public void cookieValidoDeveExecutarFlushNaResposta() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };
        PrintWriter writerMock = org.mockito.Mockito.mock(PrintWriter.class);

        when(response.getWriter()).thenReturn(writerMock);
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoLancheMock.listarTodos()).thenReturn(Arrays.asList());

        new GetLanchesTestavel().processRequest(request, response);

        verify(writerMock).flush();
    }

    @Test
    public void criarDaoLancheNaoDeveRetornarNulo() {
        GetLanchesTestavel servlet = new GetLanchesTestavel();

        assertNotNull(servlet.criarDaoLanche());
    }

    @Test
    public void criarValidadorNaoDeveRetornarNulo() {
        GetLanchesTestavel servlet = new GetLanchesTestavel();

        assertNotNull(servlet.criarValidadorCookie());
    }
}