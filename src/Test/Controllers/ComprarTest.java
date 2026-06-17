package Controllers;

import DAO.DaoBebida;
import DAO.DaoCliente;
import DAO.DaoLanche;
import DAO.DaoPedido;
import Helpers.ValidadorCookie;
import Model.Cliente;
import Model.Pedido;
import Model.Lanche;
import Model.Bebida;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComprarTest {

    @Mock
    private ValidadorCookie validadorMock;
    @Mock
    private DaoCliente daoClienteMock;
    @Mock
    private DaoLanche daoLancheMock;
    @Mock
    private DaoBebida daoBebidaMock;
    @Mock
    private DaoPedido daoPedidoMock;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private StringWriter respostaHttp;

    private class ComprarTestavel extends comprar {
        protected ValidadorCookie criarValidadorCookie() {
            return validadorMock;
        }

        protected DaoCliente criarDaoCliente() {
            return daoClienteMock;
        }

        protected DaoLanche criarDaoLanche() {
            return daoLancheMock;
        }

        protected DaoBebida criarDaoBebida() {
            return daoBebidaMock;
        }

        protected DaoPedido criarDaoPedido() {
            return daoPedidoMock;
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

    // Teste 1: cookie inválido deve retornar erro
    @Test
    public void cookieInvalidoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new ComprarTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Teste 2: cookie válido deve salvar pedido
    @Test
    public void cookieValidoDeveSalvarPedido() throws Exception {
        Cookie[] cookies = { new Cookie("token", "ok") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput("{\"id\":1}"));
        when(validadorMock.validar(cookies)).thenReturn(true);

        Cliente cliente = new Cliente();
        when(daoClienteMock.pesquisaPorID("1")).thenReturn(cliente);

        when(daoPedidoMock.pesquisaPorData(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new Pedido());

        new ComprarTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("Pedido Salvo"));
    }

    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new ComprarTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new ComprarTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new ComprarTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }

    // Forca excecao no processRequest para cobrir o tratamento de erro do doGet
    @Test
    public void doGetDeveRetornar500QuandoOcorreErro() throws Exception {
        when(request.getInputStream()).thenThrow(new IOException("falha simulada"));

        new ComprarTestavel().doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    // Forca excecao no processRequest para cobrir o tratamento de erro do doPost
    @Test
    public void doPostDeveRetornar500QuandoOcorreErro() throws Exception {
        when(request.getInputStream()).thenThrow(new IOException("falha simulada"));

        new ComprarTestavel().doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void cookieValidoComLancheEBebidaDeveSalvarPedido() throws Exception {
        Cookie[] cookies = { new Cookie("token", "ok") };

        String json = "{"
                + "\"id\":1,"
                + "\"X-Burger\":[\"X-Burger\",\"lanche\",2],"
                + "\"Coca-Cola\":[\"Coca-Cola\",\"bebida\",1]"
                + "}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(json));
        when(validadorMock.validar(cookies)).thenReturn(true);

        Cliente cliente = new Cliente();
        when(daoClienteMock.pesquisaPorID("1")).thenReturn(cliente);

        Lanche lanche = new Lanche();
        lanche.setValor_venda(18.0);
        when(daoLancheMock.pesquisaPorNome("X-Burger")).thenReturn(lanche);

        Bebida bebida = new Bebida();
        bebida.setValor_venda(6.0);
        when(daoBebidaMock.pesquisaPorNome("Coca-Cola")).thenReturn(bebida);

        Pedido pedido = new Pedido();
        when(daoPedidoMock.pesquisaPorData(org.mockito.ArgumentMatchers.any()))
                .thenReturn(pedido);

        new ComprarTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("Pedido Salvo"));
    }
}