<br />
<div align="center">
  <a href="https://github.com/othneildrew/Best-README-Template">
    <img src="https://cdn-icons-png.flaticon.com/256/5968/5968364.png" alt="Logo" width="80" height="80">
  </a>

  <h3 align="center">Sistema de Gestão de Dados das Olimpíadas - Banco de Dados em SQL Server e Interface em Java</h3>

  <p align="center">
    Este projeto visa o desenvolvimento de um sistema de gestão de dados relacionados às Olimpíadas, utilizando **Microsoft SQL Server** como banco de dados e **Java** para a interface de gestão. O objetivo é proporcionar uma plataforma eficiente para armazenar, consultar e manipular dados históricos das Olimpíadas, como resultados de disputas, medalhas conquistadas, dados de atletas, países participantes, entre outros.

A aplicação Java oferece uma interface gráfica onde os usuários podem visualizar e interagir com os dados das Olimpíadas, realizando consultas e atualizações diretamente no banco de dados.
  </p>
</div>

## Tecnologias Utilizadas

- **Banco de Dados**: Microsoft SQL Server
- **Linguagem de Programação**: Java
- **Interface Gráfica**: Swing
- **JDBC**: Para a comunicação entre a aplicação Java e o banco de dados SQL Server
- **Ferramentas**:
  - **SQL Server Management Studio (SSMS)**: Para administrar o banco de dados.
  - **JDBC Driver**: Para realizar a conexão com o SQL Server a partir da aplicação Java.

## Estrutura do Banco de Dados

O banco de dados contém informações detalhadas sobre as Olimpíadas, incluindo tabelas e relacionamentos que permitem armazenar e consultar dados de diversos tipos:

- **Atletas**: Informações sobre os atletas, como nome, país, esporte, etc.
- **Disputas**: Detalhes sobre cada disputa, como a data, o tipo de evento e os resultados.
- **Países**: Informações sobre os países participantes, incluindo medalhas conquistadas em diferentes edições.
- **Eventos e Modalidades**: Tabelas relacionadas aos eventos esportivos e modalidades disputadas durante as Olimpíadas.
- **Medalhas**: Detalhes sobre as medalhas conquistadas pelos atletas e países, com informações sobre o tipo de medalha (ouro, prata, bronze).

### Relacionamentos e Consultas

O banco de dados foi estruturado para permitir consultas complexas, como:
- Quais países conquistaram mais medalhas em determinada edição?
- Quais atletas venceram competições em múltiplas modalidades?
- Estatísticas detalhadas de cada edição das Olimpíadas, incluindo o total de medalhas e a distribuição por país e esporte.

## Funcionalidades da Aplicação Java

A aplicação Java oferece uma interface gráfica que permite ao usuário interagir com os dados das Olimpíadas de forma intuitiva. A aplicação inclui:

### Funcionalidades principais:
- **Visualização de Dados**: Exibição dos dados das Olimpíadas, como medalhas conquistadas por país, atletas e modalidades.
- **Consultas e Filtros**: O usuário pode realizar consultas avançadas, como a busca de países com mais medalhas ou atletas com maior número de vitórias.
- **Inserção e Edição de Dados**: A interface permite inserir novos registros e atualizar os existentes, como resultados de disputas ou medalhas.
  
### Tecnologias de Interface:
- **Swing**: A interface gráfica é construída utilizando essa bibliotecas para fornecer uma experiência de usuário rica e interativa.
- **JDBC**: A comunicação com o banco de dados é feita por meio de JDBC, que permite a execução de consultas e operações de dados diretamente do banco de dados SQL Server.

## Instruções de Instalação

### 1. Preparação do Banco de Dados no SQL Server

Para começar, você precisará configurar o banco de dados no **SQL Server**. Siga os passos abaixo:

1. **Instale o SQL Server**: Caso ainda não tenha o SQL Server instalado, faça o download e siga as instruções de instalação no [site oficial do Microsoft SQL Server](https://www.microsoft.com/pt-br/sql-server/sql-server-downloads)

2. **Criação do Banco de Dados**: 
   - Abra o **SQL Server Management Studio (SSMS)** ou outra ferramenta de sua preferência.
   - Crie um novo banco de dados, por exemplo, `olimpiadas_db` (ou outro nome de sua escolha).
   
3. **Execução do DDL e DML**:
   - Importe os arquivos de definição do banco de dados (DDL) e dados (DML) que estão no repositório.
     - O arquivo **DDL** contém os comandos para criar as tabelas e as estruturas do banco de dados.
     - O arquivo **DML** contém os dados que serão inseridos nas tabelas.
   - Execute os scripts no SQL Server para criar o banco de dados e popular as tabelas com os dados.

4. **Verificação**:
   - Após a execução dos scripts, verifique se as tabelas foram criadas corretamente e se os dados estão presentes.
   - Você pode rodar uma consulta simples para verificar se as tabelas estão populadas:
     ```sql
     SELECT * FROM L06_ATLETA;
     ```

### 2. Configuração da Aplicação Java

Depois de preparar o banco de dados, você pode configurar e rodar a aplicação Java. Para isso, siga as instruções detalhadas no documento de **passo a passo** que está disponibilizado no repositório. O documento contém todas as etapas necessárias para configurar o ambiente de desenvolvimento, as dependências do projeto, e a execução da aplicação Java com o banco de dados SQL Server.

1. **Clonar o Repositório**:
   - Clone o repositório para o seu ambiente local:
     ```bash
     git clone https://github.com/gianmalfate/java_manager_sql_server.git
     ```

2. **Siga as Instruções do Documento**:
   - Após clonar o repositório, acesse o documento de **Configuração - Passo a passo** e siga as instruções fornecidas para configurar a aplicação Java, as dependências, e a conexão com o banco de dados.

3. **Executar a Aplicação**:
   - Após seguir o passo a passo, compile e execute a aplicação utilizando a IDE Java de sua escolha ou diretamente via terminal.

---

**Observação:** Certifique-se de seguir todos os passos do documento cuidadosamente para garantir que o banco de dados e a aplicação Java se conectem corretamente e funcionem como esperado.


## Autores

- **Adriano Carvalho**
- **Giancarlo Malfate**
