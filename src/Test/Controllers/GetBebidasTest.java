package Controllers;

import DAO.DaoBebida;
import Helpers.ValidadorCookie;
import Model.Bebida;
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
public class GetBebidasTest {

    @Mock
    private ValidadorCookie validadorMock;
    @Mock
    private DaoBebida daoBebidaMock;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private StringWriter respostaHttp;

    private class GetBebidasTestavel extends getBebidas {
        @Override
        protected ValidadorCookie criarValidadorCookie() {
            return validadorMock;
        }

        @Override
        protected DaoBebida criarDaoBebida() {
            return daoBebidaMock;
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
        when(daoBebidaMock.listarTodos()).thenReturn(Arrays.asList());

        new GetBebidasTestavel().processRequest(request, response);

        verify(daoBebidaMock, times(1)).listarTodos();
    }

    // Teste 2: cookie inválido NÃO deve chamar listarTodos()
    @Test
    public void cookieInvalidoNaoDeveChamarListarTodos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetBebidasTestavel().processRequest(request, response);

        verify(daoBebidaMock, never()).listarTodos();
    }

    // Teste 3: cookie inválido deve retornar a mensagem "erro"
    @Test
    public void cookieInvalidoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetBebidasTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Teste 4: cookie válido NÃO deve retornar a mensagem "erro"
    @Test
    public void cookieValidoNaoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoBebidaMock.listarTodos()).thenReturn(Arrays.asList());

        new GetBebidasTestavel().processRequest(request, response);

        assertFalse(respostaHttp.toString().contains("erro"));
    }

    // Teste 5: cookie válido deve retornar o nome da bebida no JSON
    @Test
    public void cookieValidoDeveRetornarNomeDaBebida() throws Exception {
        Bebida bebida = new Bebida();
        bebida.setNome("Coca-Cola");

        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };
        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoBebidaMock.listarTodos()).thenReturn(Arrays.asList(bebida));

        new GetBebidasTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("Coca-Cola"));
    }

    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetBebidasTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetBebidasTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new GetBebidasTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }
}
