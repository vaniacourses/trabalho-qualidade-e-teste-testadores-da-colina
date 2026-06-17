package Controllers;

import DAO.DaoIngrediente;
import DAO.DaoLanche;
import Helpers.ValidadorCookie;
import Model.Ingrediente;
import Model.Lanche;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SalvarLancheClienteTest {

    @Mock
    private ValidadorCookie validadorMock;
    @Mock
    private DaoIngrediente daoIngredienteMock;
    @Mock
    private DaoLanche daoLancheMock;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private StringWriter respostaHttp;

    private class SalvarLancheTestavel extends salvarLancheCliente {
        protected ValidadorCookie criarValidadorCookie() {
            return validadorMock;
        }

        protected DaoIngrediente criarDaoIngrediente() {
            return daoIngredienteMock;
        }

        protected DaoLanche criarDaoLanche() {
            return daoLancheMock;
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
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new SalvarLancheTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void cookieValidoDeveRetornarCarrinho() throws Exception {
        Cookie[] cookies = { new Cookie("token", "ok") };

        String json = "{\"nome\":\"X\",\"descricao\":\"Teste\",\"ingredientes\":{\"Queijo\":1}}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(json));
        when(validadorMock.validar(cookies)).thenReturn(true);

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setValor_venda(5.0);

        when(daoIngredienteMock.pesquisaPorNome(org.mockito.ArgumentMatchers.any(Ingrediente.class)))
                .thenReturn(ingrediente);

        Lanche lanche = new Lanche();
        lanche.setNome("X");
        lanche.setValor_venda(5.0);

        when(daoLancheMock.pesquisaPorNome(org.mockito.ArgumentMatchers.any(Lanche.class)))
                .thenReturn(lanche);

        new SalvarLancheTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("carrinho"));
    }

    @Test
    public void semCookieDeveRetornarErro() throws Exception {
        when(request.getCookies()).thenReturn(null);
        when(request.getInputStream()).thenReturn(criarInput(""));

        new SalvarLancheTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void cookieValidoComDoisIngredientesDeveRetornarCarrinho() throws Exception {
        Cookie[] cookies = { new Cookie("token", "ok") };

        String json = "{\"nome\":\"X-Tudo\",\"descricao\":\"Teste\",\"ingredientes\":{\"Queijo\":1,\"Bacon\":1}}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(json));
        when(validadorMock.validar(cookies)).thenReturn(true);

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setValor_venda(5.0);

        when(daoIngredienteMock.pesquisaPorNome(org.mockito.ArgumentMatchers.any(Ingrediente.class)))
                .thenReturn(ingrediente);

        Lanche lanche = new Lanche();
        lanche.setNome("X-Tudo");
        lanche.setValor_venda(10.0);

        when(daoLancheMock.pesquisaPorNome(org.mockito.ArgumentMatchers.any(Lanche.class)))
                .thenReturn(lanche);

        new SalvarLancheTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("carrinho"));
    }

    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new SalvarLancheTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new SalvarLancheTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new SalvarLancheTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }
}