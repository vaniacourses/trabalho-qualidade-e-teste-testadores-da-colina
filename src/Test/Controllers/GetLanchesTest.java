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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GetLanchesTest {

    @Mock
    private ValidadorCookie validadorMock;
    @Mock
    private DaoLanche daoLancheMock;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private StringWriter respostaHttp;

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

    @Before
    public void configurar() throws Exception {
        respostaHttp = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(respostaHttp));
    }

    // Teste 1: cookie válido deve chamar listarTodos() uma vez
    @Test
    public void cookieValidoDeveChamarListarTodos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoLancheMock.listarTodos()).thenReturn(Arrays.asList());

        new GetLanchesTestavel().processRequest(request, response);

        verify(daoLancheMock, times(1)).listarTodos();
    }

    // Teste 2: cookie inválido NÃO deve chamar listarTodos()
    @Test
    public void cookieInvalidoNaoDeveChamarListarTodos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetLanchesTestavel().processRequest(request, response);

        verify(daoLancheMock, never()).listarTodos();
    }

    // Teste 3: cookie inválido deve retornar a mensagem "erro"
    @Test
    public void cookieInvalidoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetLanchesTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Teste 4: cookie válido NÃO deve retornar a mensagem "erro"
    @Test
    public void cookieValidoNaoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoLancheMock.listarTodos()).thenReturn(Arrays.asList());

        new GetLanchesTestavel().processRequest(request, response);

        assertFalse(respostaHttp.toString().contains("erro"));
    }

    // Teste 5: cookie válido deve retornar o nome do lanche no JSON
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

    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetLanchesTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetLanchesTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new GetLanchesTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }
}
