![unifor](/imgs/unifor5.png)

UNIVERSIDADE DE FORTALEZA

CENTRO DE CIÊNCIAS TECNOLÓGICAS

CURSO: CIÊNCIA DA COMPUTAÇÃO

# SIMULADOR DE SISTEMA DE ARQUIVO

Autor 1: João Victor Rodrigues Barreto (Matrícula: 2422976)

Autor 2: Nicolas Marcelino da Mota (Matrícula: 2420508)

Palavras-chaves: File. System. Archives. Directory.

Link para o README: https://github.com/nicolasmarcelino/FileSystemSimulator/blob/main/README.md

Link para o repositório do projeto: https://github.com/nicolasmarcelino/FileSystemSimulator

# RESUMO

O projeto implementa um simulador de sistema de arquivos hierárquico em Java, operado por meio de um shell interativo em linha de comando. O sistema permite criar diretórios e arquivos, navegar pela árvore e persistir o estado em disco. Cada operação que altera a estrutura é registrada em um *journal* (diário) seguindo a técnica de *write-ahead logging*, o que permite identificar e recuperar operações interrompidas por falhas.

# INTRODUÇÃO

## O que é um sistema de arquivos
 
O sistema de arquivos é a parte do sistema operacional responsável por organizar, armazenar e recuperar informações em memória secundária de forma persistente. Ele oferece ao usuário a abstração de **arquivo** (uma unidade nomeada de informação) e de **diretório** (um agrupamento de arquivos e de outros diretórios), formando normalmente uma árvore cuja origem é o diretório raiz. Cada arquivo ou diretório possui nome e metadados (como a data de criação), e é localizado por um **caminho** (*path*), que descreve a sequência de diretórios a partir da raiz.
 
Entre as responsabilidades de um sistema de arquivos estão: a criação, remoção e renomeação de arquivos e diretórios; a navegação e listagem da hierarquia; o controle de metadados; e a garantia de que os dados continuem disponíveis após o desligamento da máquina.

## O que é o journaling
 
Operações de um sistema de arquivos raramente são atômicas no disco. Criar um arquivo, por exemplo, pode exigir alterar a entrada do diretório pai, alocar uma estrutura de controle e atualizar mapas de espaço livre. Se ocorrer uma queda de energia ou uma falha no meio dessas etapas, o sistema pode ficar **inconsistente**: um diretório apontando para um arquivo que não existe, ou espaço marcado como usado sem pertencer a ninguém. Sem proteção, a recuperação exige varrer todo o disco (como faz o `fsck`), o que é lento em volumes grandes.
 
O **journaling** resolve esse problema mantendo um registro sequencial (*journal* ou *log*) das operações que serão feitas, **antes** de elas serem aplicadas à estrutura principal. Após uma falha, basta ler o journal: operações concluídas (*commit*) podem ser reaplicadas, e operações incompletas (sem *commit*) são descartadas ou desfeitas. A recuperação passa a depender do tamanho do log, e não do tamanho do disco.
 
O journaling se integra ao sistema de arquivos como uma camada de proteção em torno de cada operação que modifica a estrutura: a operação é anunciada no log (`BEGIN`), executada, e então confirmada (`COMMIT`) ou cancelada (`ABORT`). O log é escrito de forma síncrona, forçando a gravação física, para que sobreviva a uma falha.

# METODOLOGIA

O simulador foi desenvolvido em Java, utilizando apenas a biblioteca padrão (sem dependências externas). O código utiliza *records* e *switch* com expressões em seta, o que exige JDK 17 ou superior. O sistema de arquivos é mantido em memória como uma árvore de objetos e persistido em disco por serialização Java; o journal é um arquivo de texto de escrita incremental.

## Classes do projeto
 
| Classe | Função |
|---|---|
| `FileType` | Classe abstrata base de tudo o que existe no sistema de arquivos. Guarda nome, data de criação e referência ao diretório pai, e calcula o caminho completo. É serializável. |
| `Arquivo` | Representa um arquivo comum. Estende `FileType` e acrescenta o conteúdo textual. |
| `Diretorio` | Representa um diretório. Estende `FileType` e mantém a lista de filhos (arquivos e subdiretórios), com operações de adicionar, remover e buscar. |
| `FileSystem` | Núcleo do sistema de arquivos. Mantém o diretório raiz, cria arquivos e diretórios, lista e busca conteúdo e cuida da persistência (salvar e carregar o estado em disco). |
| `Journal` | Gerencia o diário de operações: registra início, confirmação e cancelamento de cada operação e lê o log existente na inicialização. |
| `JournalEntry` | *Record* que representa uma linha do journal (identificador, data e hora, status, operação e parâmetros). |
| `Shell` | Interface com o usuário. Lê comandos do terminal, aciona o `FileSystem` e envolve cada operação de modificação com o `Journal`. |

## Implementação do journaling
 
O journaling segue o modelo de **write-ahead logging** aplicado às operações de modificação da estrutura (criação de diretórios e arquivos). Todo o registro é feito no arquivo `journal.log`, em formato texto, com uma entrada por linha e campos separados por `|`:
 
```
id|timestamp|status|operacao|param1|param2|...
```
 
Exemplo de trecho do log:
 
```
1|2025-01-10T14:32:05.123|BEGIN|CREATE_DIR|/root|docs
1|2025-01-10T14:32:05.130|COMMIT|
2|2025-01-10T14:32:20.455|BEGIN|CREATE_FILE|/root/docs|notas.txt
2|2025-01-10T14:32:20.461|COMMIT|
```
 
O ciclo de vida de uma operação é o seguinte:
 
1. **`BEGIN`**: antes de alterar a árvore, o `Shell` chama `journal.begin(operacao, parametros)`. O journal atribui um identificador sequencial, grava a intenção da operação (tipo e parâmetros, como diretório atual e nome) e retorna o `id`.
2. **Execução**: o `FileSystem` aplica a operação sobre a árvore em memória.
3. **`COMMIT`**: se a operação terminou sem erro, `journal.commit(id)` grava a confirmação.
4. **`ABORT`**: se ocorreu uma exceção, `journal.abort(id)` registra o cancelamento e o erro é propagado.

## Como executar
 
### Compilação e execução
 
Na raiz do projeto, compile todas as classes e inicie o shell:
 
```bash
javac *.java
java Shell
```
 
Os arquivos `filesystem.dat` e `journal.log` são criados no diretório onde o comando é executado. Execute sempre a partir do mesmo diretório para reaproveitar o estado salvo.

Para que as alterações persistam entre execuções, o programa deve ser encerrado com `exit`.

# RESULTADOS E DISCUSSÕES

 
O simulador reproduz o comportamento básico de um sistema de arquivos hierárquico: a árvore é criada, navegada e listada corretamente pelo shell, e o estado é restaurado em uma nova execução a partir de `filesystem.dat`. A separação em classes (`FileType`, `Arquivo` e `Diretorio`) permite tratar arquivos e diretórios de forma uniforme, o que simplifica a listagem e o cálculo de caminhos.
 
Quanto ao journaling, cada operação de criação gera um par de linhas no `journal.log` (`BEGIN` seguido de `COMMIT` ou `ABORT`), o que torna possível distinguir, após uma falha, o que foi concluído do que ficou pela metade. A escrita síncrona do log tem custo de desempenho, pois cada entrada exige uma sincronização com o disco, mas é justamente esse custo que garante a durabilidade do registro. Esse é o mesmo compromisso encontrado em sistemas reais: pagar uma pequena escrita sequencial extra em troca de uma recuperação rápida e consistente.
 
Como simplificação em relação a sistemas reais, o journal registra apenas operações lógicas sobre metadados (similar ao journaling de metadados) e o conteúdo dos arquivos não passa por ele. Além disso, o salvamento completo da árvore a cada `exit` equivale a um *checkpoint* total, e a ofuscação por XOR serve apenas para fins didáticos, não sendo um mecanismo de segurança.

# CONCLUSÕES

 
O trabalho resultou em um simulador em Java de um sistema de arquivos com journaling, composto por uma hierarquia de classes simples, um shell interativo e um diário de operações baseado em *write-ahead logging*. A implementação demonstra na prática por que o registro prévio das operações, com confirmação explícita, permite recuperar o sistema após falhas sem varrer toda a estrutura. A arquitetura modular facilita futuras extensões, como novos tipos de operação (renomear e remover arquivos e diretórios), a conversão de status e operações em enumerações e a evolução para outros modelos de journaling.

# REFERÊNCIAS

TANENBAUM, A. S.; BOS, H. Sistemas operacionais modernos. 4. ed. São Paulo: Pearson, 2015.