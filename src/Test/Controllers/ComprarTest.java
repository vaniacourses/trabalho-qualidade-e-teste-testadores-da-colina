package Controllers;

// Imports dos DAOs usados no teste
import DAO.DaoBebida;
import DAO.DaoCliente;
import DAO.DaoLanche;
import DAO.DaoPedido;

// Import do validador de cookie
import Helpers.ValidadorCookie;

// Imports dos models usados
import Model.Bebida;
import Model.Cliente;
import Model.Lanche;
import Model.Pedido;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Permite usar Mockito no teste
@RunWith(MockitoJUnitRunner.class)
public class ComprarTest {

    // Mocks das dependências do servlet
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

    // Guarda a resposta escrita pelo servlet
    private StringWriter respostaHttp;

    // Classe usada para trocar os DAOs reais por mocks
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

    // Cria um InputStream falso para simular o JSON da requisição
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

    // Executa antes de cada teste
    @Before
    public void configurar() throws Exception {
        respostaHttp = new StringWriter();

        // Faz o response escrever dentro da variável respostaHttp
        when(response.getWriter()).thenReturn(new PrintWriter(respostaHttp));
    }

    // Testa se cookie inválido retorna erro
    @Test
    public void cookieInvalidoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new ComprarTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se cookie válido salva um pedido sem itens
    @Test
    public void cookieValidoDeveSalvarPedido() throws Exception {
        Cookie[] cookies = { new Cookie("token", "ok") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput("{\"id\":1}"));
        when(validadorMock.validar(cookies)).thenReturn(true);

        Cliente cliente = new Cliente();
        when(daoClienteMock.pesquisaPorID("1")).thenReturn(cliente);

        Pedido pedidoRetornado = new Pedido();

        when(daoPedidoMock.pesquisaPorData(any(Pedido.class)))
                .thenReturn(pedidoRetornado);
        new ComprarTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("Pedido Salvo"));

        // Captura o pedido enviado para o método salvar
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);

        verify(daoPedidoMock, times(1)).salvar(captor.capture());
        verify(daoPedidoMock, times(1)).pesquisaPorData(any(Pedido.class));

        Pedido pedidoSalvo = captor.getValue();

        // Verifica se o cliente e o valor total estão corretos
        assertEquals(cliente, pedidoSalvo.getCliente());
        assertEquals(0.0, pedidoSalvo.getValor_total(), 0.01);
        assertNotNull(pedidoSalvo.getData_pedido());
        assertEquals(cliente, pedidoRetornado.getCliente());
    }

    // Testa se pedido com lanche e bebida calcula o valor e cria os vínculos
    @Test
    public void cookieValidoComLancheEBebidaDeveSalvarPedidoComValorTotalEVinculos() throws Exception {
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

        Pedido pedidoRetornado = new Pedido();
        when(daoPedidoMock.pesquisaPorData(any(Pedido.class))).thenReturn(pedidoRetornado);

        new ComprarTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("Pedido Salvo"));

        // Captura o pedido salvo para verificar os dados
        ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);

        verify(daoPedidoMock, times(1)).salvar(captor.capture());
        verify(daoPedidoMock, times(1)).pesquisaPorData(any(Pedido.class));

        // Verifica se os vínculos com lanche e bebida foram feitos
        verify(daoPedidoMock, times(1)).vincularLanche(any(Pedido.class), any(Lanche.class));
        verify(daoPedidoMock, times(1)).vincularBebida(any(Pedido.class), any(Bebida.class));

        Pedido pedidoSalvo = captor.getValue();

        // Verifica os valores finais do pedido
        assertEquals(cliente, pedidoSalvo.getCliente());
        assertEquals(24.0, pedidoSalvo.getValor_total(), 0.01);
        assertEquals(2, lanche.getQuantidade());
        assertEquals(1, bebida.getQuantidade());
        assertEquals(cliente, pedidoRetornado.getCliente());
    }

    // Testa se o doPost chama o processRequest
    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new ComprarTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se o doGet chama o processRequest
    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new ComprarTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa a descrição do servlet
    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new ComprarTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }

    @Test
    public void deveConfigurarResponseQuandoProcessaRequisicao() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new ComprarTestavel().processRequest(request, response);

        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
    }

    @Test
    public void doGetDeveRetornar500QuandoOcorreErro() throws Exception {
        when(request.getInputStream()).thenThrow(new IOException("falha simulada"));

        new ComprarTestavel().doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void doPostDeveRetornar500QuandoOcorreErro() throws Exception {
        when(request.getInputStream()).thenThrow(new IOException("falha simulada"));

        new ComprarTestavel().doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}