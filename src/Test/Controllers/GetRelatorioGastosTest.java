package Controllers;

import DAO.DaoRelatorio;
import Helpers.ValidadorCookie;
import Model.RelatorioGastos;
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
import static org.mockito.Mockito.*;

// Permite usar Mockito nos testes
@RunWith(MockitoJUnitRunner.class)
public class GetRelatorioGastosTest {

    // Mocks das dependências usadas pelo servlet
    @Mock
    private ValidadorCookie validadorMock;

    @Mock
    private DaoRelatorio daoRelatorioMock;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    // Guarda a resposta escrita pelo servlet
    private StringWriter respostaHttp;

    // Classe testável para substituir dependências reais por mocks
    private class GetRelatorioGastosTestavel extends getRelatorioGastos {
        @Override
        protected ValidadorCookie criarValidadorCookie() {
            return validadorMock;
        }

        @Override
        protected DaoRelatorio criarDaoRelatorio() {
            return daoRelatorioMock;
        }
    }

    // Executa antes de cada teste
    @Before
    public void configurar() throws Exception {
        respostaHttp = new StringWriter();

        // Faz o response escrever dentro da variável respostaHttp
        when(response.getWriter()).thenReturn(new PrintWriter(respostaHttp));
    }

    // Testa se cookie válido chama listarRelGastos()
    @Test
    public void cookieValidoDeveChamarListarRelGastos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoRelatorioMock.listarRelGastos()).thenReturn(Arrays.asList());

        new GetRelatorioGastosTestavel().processRequest(request, response);

        verify(daoRelatorioMock, times(1)).listarRelGastos();
    }

    // Testa se cookie inválido não chama listarRelGastos()
    @Test
    public void cookieInvalidoNaoDeveChamarListarRelGastos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetRelatorioGastosTestavel().processRequest(request, response);

        verify(daoRelatorioMock, never()).listarRelGastos();
    }

    // Testa se cookie inválido retorna erro
    @Test
    public void cookieInvalidoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetRelatorioGastosTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se cookie válido não retorna erro
    @Test
    public void cookieValidoNaoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoRelatorioMock.listarRelGastos()).thenReturn(Arrays.asList());

        new GetRelatorioGastosTestavel().processRequest(request, response);

        assertFalse(respostaHttp.toString().contains("erro"));
    }

    // Testa se o JSON retorna os dados do relatório
    @Test
    public void cookieValidoDeveRetornarDadosDoRelatorioEmJson() throws Exception {
        RelatorioGastos relatorio = new RelatorioGastos();
        relatorio.setCusto(100.0f);
        relatorio.setVenda(150.0f);
        relatorio.setLucro(50.0f);

        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoRelatorioMock.listarRelGastos()).thenReturn(Arrays.asList(relatorio));

        new GetRelatorioGastosTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("100.0"));
        assertTrue(respostaHttp.toString().contains("150.0"));
        assertTrue(respostaHttp.toString().contains("50.0"));
    }

    // Testa se o doGet chama o processRequest
    @Test
    public void doGetDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetRelatorioGastosTestavel().doGet(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa se o doPost chama o processRequest
    @Test
    public void doPostDeveChamarProcessRequest() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetRelatorioGastosTestavel().doPost(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }

    // Testa a descrição do servlet
    @Test
    public void deveRetornarDescricaoDoServlet() {
        String descricao = new GetRelatorioGastosTestavel().getServletInfo();

        assertTrue(descricao.contains("Short description"));
    }

    // Testa se o response foi configurado como JSON UTF-8
    @Test
    public void deveConfigurarResponseQuandoProcessaRequisicao() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoRelatorioMock.listarRelGastos()).thenReturn(Arrays.asList());

        new GetRelatorioGastosTestavel().processRequest(request, response);

        verify(response, atLeastOnce()).setContentType("application/json");
        verify(response, atLeastOnce()).setCharacterEncoding("UTF-8");
    }
}