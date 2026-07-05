package Controllers;

import DAO.DaoIngrediente;
import DAO.DaoLanche;
import Helpers.ValidadorCookie;
import Model.Ingrediente;
import Model.Lanche;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Permite usar Mockito no teste
@RunWith(MockitoJUnitRunner.class)
public class SalvarLancheClienteTest {

    // Mocks das dependências usadas pelo servlet
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

    // Guarda a resposta escrita pelo servlet
    private StringWriter respostaHttp;

    // Classe testável para substituir dependências reais por mocks
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

    // Cria um input falso para simular o JSON da requisição
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

        new SalvarLancheTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se a ausência de cookie retorna erro
    @Test
    public void semCookieDeveRetornarErro() throws Exception {
        when(request.getCookies()).thenReturn(null);
        when(request.getInputStream()).thenReturn(criarInput(""));

        new SalvarLancheTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se cookie válido salva lanche com um ingrediente
    @Test
    public void cookieValidoDeveRetornarCarrinho() throws Exception {
        Cookie[] cookies = { new Cookie("token", "ok") };

        String json = "{\"nome\":\"X\",\"descricao\":\"Teste\",\"ingredientes\":{\"Queijo\":1}}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(json));
        when(validadorMock.validar(cookies)).thenReturn(true);

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setValor_venda(5.0);

        // Retorna o ingrediente quando ele for pesquisado
        when(daoIngredienteMock.pesquisaPorNome(any(Ingrediente.class)))
                .thenReturn(ingrediente);

        Lanche lanche = new Lanche();

        // Retorna o lanche quando ele for pesquisado
        when(daoLancheMock.pesquisaPorNome(any(Lanche.class)))
                .thenReturn(lanche);

        new SalvarLancheTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("carrinho"));

        // Captura o lanche enviado para salvarCliente
        ArgumentCaptor<Lanche> captorLanche = ArgumentCaptor.forClass(Lanche.class);

        verify(daoLancheMock, times(1)).salvarCliente(captorLanche.capture());

        Lanche lancheSalvo = captorLanche.getValue();

        // Verifica se os dados do lanche foram preenchidos corretamente
        assertEquals("X", lancheSalvo.getNome());
        assertEquals("Teste", lancheSalvo.getDescricao());
        assertEquals(5.0, lancheSalvo.getValor_venda(), 0.01);
    }

    // Testa se cookie válido salva lanche com dois ingredientes
    @Test
    public void cookieValidoComDoisIngredientesDeveRetornarCarrinho() throws Exception {
        Cookie[] cookies = { new Cookie("token", "ok") };

        String json = "{\"nome\":\"X-Tudo\",\"descricao\":\"Teste\",\"ingredientes\":{\"Queijo\":1,\"Bacon\":2}}";

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(json));
        when(validadorMock.validar(cookies)).thenReturn(true);

        Ingrediente ingrediente = new Ingrediente();
        ingrediente.setValor_venda(5.0);

        // Simula a busca de ingredientes pelo DAO
        when(daoIngredienteMock.pesquisaPorNome(any(Ingrediente.class)))
                .thenReturn(ingrediente);

        Lanche lanche = new Lanche();

        // Simula a busca do lanche salvo
        when(daoLancheMock.pesquisaPorNome(any(Lanche.class)))
                .thenReturn(lanche);

        new SalvarLancheTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("carrinho"));

        // Captura o lanche salvo
        ArgumentCaptor<Lanche> captorLanche = ArgumentCaptor.forClass(Lanche.class);

        verify(daoLancheMock, times(1)).salvarCliente(captorLanche.capture());
        verify(daoLancheMock, times(1)).pesquisaPorNome(any(Lanche.class));
        // Verifica se os dois ingredientes foram vinculados ao lanche
        verify(daoLancheMock, times(2))
                .vincularIngrediente(any(Lanche.class), any(Ingrediente.class));

        Lanche lancheSalvo = captorLanche.getValue();

        // Verifica se o valor total foi calculado corretamente
        assertEquals("X-Tudo", lancheSalvo.getNome());
        assertEquals("Teste", lancheSalvo.getDescricao());
        assertEquals(15.0, lancheSalvo.getValor_venda(), 0.01);
    }

    // Testa se o doPost chama o processRequest
    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new SalvarLancheTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se o doGet chama o processRequest
    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("token", "x") };

        when(request.getCookies()).thenReturn(cookies);
        when(request.getInputStream()).thenReturn(criarInput(""));
        when(validadorMock.validar(cookies)).thenReturn(false);

        new SalvarLancheTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa a descrição do servlet
    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new SalvarLancheTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }

    // Forca excecao no processRequest para cobrir o tratamento de erro do doGet
    @Test
    public void doGetDeveRetornar500QuandoOcorreErro() throws Exception {
        when(request.getInputStream()).thenThrow(new IOException("falha simulada"));

        new SalvarLancheTestavel().doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    // Forca excecao no processRequest para cobrir o tratamento de erro do doPost
    @Test
    public void doPostDeveRetornar500QuandoOcorreErro() throws Exception {
        when(request.getInputStream()).thenThrow(new IOException("falha simulada"));

        new SalvarLancheTestavel().doPost(request, response);

        verify(response).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
}