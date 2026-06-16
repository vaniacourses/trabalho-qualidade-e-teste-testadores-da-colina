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

@RunWith(MockitoJUnitRunner.class)
public class GetRelatorioGastosTest {

    @Mock private ValidadorCookie validadorMock;
    @Mock private DaoRelatorio daoRelatorioMock;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    private StringWriter respostaHttp;

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

    @Before
    public void configurar() throws Exception {
        respostaHttp = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(respostaHttp));
    }
    
    // Teste 1: Deve consultar o relatório quando o cookie for válido
    @Test
    public void cookieValidoDeveChamarListarRelGastos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoRelatorioMock.listarRelGastos()).thenReturn(Arrays.asList());

        new GetRelatorioGastosTestavel().processRequest(request, response);

        verify(daoRelatorioMock, times(1)).listarRelGastos();
    }
    
    // Teste 2: Não deve consultar o relatório quando o cookie for inválido
    @Test
    public void cookieInvalidoNaoDeveChamarListarRelGastos() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetRelatorioGastosTestavel().processRequest(request, response);

        verify(daoRelatorioMock, never()).listarRelGastos();
    }
    
    // Teste 3: Deve retornar mensagem de erro quando acesso for negado
    @Test
    public void cookieInvalidoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "invalido") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(false);

        new GetRelatorioGastosTestavel().processRequest(request, response);

        assertTrue(respostaHttp.toString().contains("erro"));
    }
    // Teste 4: Não deve retornar erro quando acesso for autorizado
    @Test
    public void cookieValidoNaoDeveRetornarErro() throws Exception {
        Cookie[] cookies = { new Cookie("tokenFuncionario", "abc") };

        when(request.getCookies()).thenReturn(cookies);
        when(validadorMock.validarFuncionario(cookies)).thenReturn(true);
        when(daoRelatorioMock.listarRelGastos()).thenReturn(Arrays.asList());

        new GetRelatorioGastosTestavel().processRequest(request, response);

        assertFalse(respostaHttp.toString().contains("erro"));
    }
    // Teste 5: Deve retornar os dados do relatório em formato JSON
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
}