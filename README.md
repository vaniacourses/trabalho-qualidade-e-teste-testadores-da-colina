# Trabalho de Qualidade e Teste de Software — Testadores da Colina

Repositório do trabalho da disciplina **Qualidade e Teste de Software (A1)** — UFF.  
Sistema sob teste: **Lanchonete Online ("Cade Burger's")**.

**Equipe:** Brenda de Souza, Carlos Eduardo Alves, Paulo Rodrigo Figueiredo e Yuri Moura

## 📋 Artefatos de QA

| Artefato | Localização |
|---|---|
| 📄 **Plano de Testes** (Google Docs) | [Abrir documento](https://docs.google.com/document/d/1sJ6BjGiADJPM8KH4UFqo3KPaeLkM1Vod_awEb3Hu6Bg/edit?usp=sharing) |
| 🧪 **Testes unitários** (JUnit 4 + Mockito) | [`src/Test/Controllers`](src/Test/Controllers) |
| 🐞 **Defeitos** (bug tracker) | [GitHub Issues](../../issues) |
| 📝 **Documento de Bugs/Issues** (Google Docs) | [Abrir documento](https://docs.google.com/document/d/1hVdckAPw-DxUisKo2MTNSwM5CSh48TGALNI98EbPlsM/edit?usp=sharing) |
| ✅ **Evidências de teste manual** (TestLink) | Disponíveis na seção *Testes Manuais* do Plano de Testes |

## 📊 Resultados dos Testes

### Cobertura Estrutural (JaCoCo)

Foram desenvolvidos testes unitários utilizando **JUnit 4** e **Mockito** para classes do pacote `Controllers`, priorizando componentes com lógica mais complexa.

Principais resultados:

| Classe | Cobertura de Instruções | Cobertura de Ramos |
|----------|:----------------------:|:-----------------:|
| `cadastro` | 93% | 50% |
| `comprar` | 92% | 93% |
| `salvarLancheCliente` | 81% | 75% |
| `salvarIngrediente` | 84% | 75% |
| `getLanches` | 84% | 100% |
| `getBebidas` | 84% | 100% |
| `getRelatorioGastos` | 84% | 100% |
| `login` | 83% | 83% |
| `loginFuncionario` | 83% | 83% |

### 🧬 Teste Baseado em Defeitos (PIT Mutation Testing)

Os testes também foram avaliados utilizando a ferramenta **PIT Mutation Testing**, responsável por inserir mutações artificiais no código e verificar se elas são detectadas pelos testes.

Resultados obtidos:

- **Line Coverage:** 89% (193/216)
- **Mutation Coverage:** 65% (58/89)
- **Test Strength:** 84% (58/69)

O valor de **Test Strength** obtido foi superior à meta mínima de 80%, indicando boa capacidade dos testes em detectar defeitos.

## 🧪 Casos de Teste Unitários

Os testes cobrem classes com lógica mais complexa do sistema:

- `LoginTest` e `LoginFuncionarioTest` — autenticação.
- `CadastroTest` — cadastro de cliente e endereço.
- `ComprarTest` — fluxo de compra e finalização do pedido.
- `SalvarLancheClienteTest` — criação de lanche personalizado.
- `SalvarIngredienteTest` — cadastro de ingredientes.
- `GetBebidasTest` — consulta das bebidas cadastradas.
- `GetLanchesTest` — consulta dos lanches cadastrados.
- `GetRelatorioGastosTest` — geração de relatórios.

Para executar os testes:

```bash
mvn test
```

Para executar a análise de mutação:

```bash
mvn org.pitest:pitest-maven:mutationCoverage
```

## 🍔 Sobre o Sistema

O sistema **Lanchonete Online ("Cade Burger's")** permite que clientes realizem pedidos e montem lanches personalizados, enquanto administradores podem gerenciar produtos e acompanhar relatórios.

A aplicação foi desenvolvida utilizando Java Web, Servlets, HTML, CSS, JavaScript e PostgreSQL.

## 🙏 Créditos

Este trabalho foi desenvolvido a partir de um projeto-base de uma aplicação de lanchonete online disponibilizado anteriormente para fins acadêmicos.

A equipe **Testadores da Colina** realizou adaptações e acrescentou os artefatos de qualidade de software, incluindo:

- Plano de testes;
- Casos de teste unitários;
- Testes de integração;
- Evidências de testes manuais;
- Análise de cobertura com JaCoCo;
- Testes baseados em defeitos com PIT Mutation Testing;
- Registro e documentação de defeitos.

## 🖼️ Screenshots

![Tela 1](https://i.ibb.co/BPn99jW/248f5162-df3a-4754-8ade-82b9784f94d8.jpg)
![Tela 2](https://i.ibb.co/GM3r7Dd/daf6e1f9-676e-4a27-9669-80036dc52cce.jpg)
![Tela 3](https://i.ibb.co/kXdFFq5/e378bda9-bcc8-4483-bb2f-f2143a79817e.jpg)
![Tela 4](https://i.ibb.co/z7kqx4x/a5a0e3f3-3605-4d3f-b2ba-f54c2ef76f18.jpg)
![Tela 5](https://i.ibb.co/C6kMZLW/c1bad7f9-c79a-4516-9d08-bc2548ee9880.jpg)
![Tela 6](https://i.ibb.co/2321674/8a74fb26-1db0-49df-b2d7-2479d0567a4e.jpg)
![Tela 7](https://i.ibb.co/2YSbvGZ/8d3386e3-d13b-4a42-b389-151fbadb1d77.jpg)
