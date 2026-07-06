package Controllers;

import DAO.DaoIngrediente;
import Helpers.ValidadorCookie;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SalvarIngredienteTest {

    @Mock
    private ValidadorCookie validadorMock;
    @Mock
    private DaoIngrediente daoIngredienteMock;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private StringWriter respostaHttp;

    private class SalvarIngredienteTestavel extends salvarIngrediente {
        protected ValidadorCookie criarValidadorCookie() {
            return validadorMock;
        }

        protected DaoIngrediente criarDaoIngrediente() {
            return daoIngredienteMock;
        }
    }

    private ServletInputStream criarInput(String json) {
        ByteArrayInputStream entrada = new ByteArrayInputStream(json.getBytes());

        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return entrada.read();
            }

            @Override
            public boolean isFinished() {
                return entrada.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }
        };
    }

    @Before
    public void configurar() throws Exception {
        respostaHttp = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(respostaHttp));
    }

    @Test
    public void cookieInvalidoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new SalvarIngredienteTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void cookieValidoDeveSalvarIngrediente() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "ok") };

        String json = "{\"nome\":\"Queijo\",\"descricao\":\"Teste\",\"quantidade\":10,\"ValorCompra\":2,\"ValorVenda\":5,\"tipo\":\"extra\"}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(json));
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);

        new SalvarIngredienteTestavel().processRequest(request, response);

        verify(daoIngredienteMock, times(1)).salvar(any());

        assertTrue(respostaHttp.toString().contains("Ingrediente Salvo"));
    }

    @Test
    public void semCookieDeveRetornarErro() throws Exception {
        when(request.getCookies()).thenReturn(null);
        when(request.getInputStream()).thenReturn(criarInput(""));

        new SalvarIngredienteTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void excecaoAoLerCookiesDeveRetornarErro() throws Exception {
        when(request.getCookies()).thenThrow(new NullPointerException());
        when(request.getInputStream()).thenReturn(criarInput(""));

        new SalvarIngredienteTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new SalvarIngredienteTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new SalvarIngredienteTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new SalvarIngredienteTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }

    @Test
    public void semCorpoNaRequisicaoDeveRetornarErro() throws Exception {
        when(request.getInputStream()).thenReturn(null);

        new SalvarIngredienteTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }
}