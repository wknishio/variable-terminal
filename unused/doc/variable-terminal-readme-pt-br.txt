Variable-Terminal v1.4.2 by wknishio@gmail.com

###### Visão Geral ######

O Variable-Terminal é um aplicativo Java experimental focado em administração
remota de computadores. É quase uma mistura entre servidor/cliente TELNET
com servidor/cliente de acesso remoto gráfico, junto com algumas outras
funcionalidades remotas inclusas. Ainda que possua funcionalidades parecidas
com aplicativos TELNET e VNC/RDP, na prática o Variable-Terminal não implementa
nenhum desses três protocolos.

O autor do software Variable-Terminal não dá suporte nem garantia alguma sobre o
funcionamento deste aplicativo experimental tampouco se responsabiliza por
qualquer tipo de dano que possa ocorrer decorrente do uso do mesmo.
USE POR SUA CONTA E RISCO!

Variable-Terminal precisa de pelo menos Java 1.5 para funcionar. Recomenda-se pelo
menos Java 1.6, devido a diversas otimizações de performance implementadas
nessa versão (no JRE distribuído pela Sun Microsystems).

Variable-Terminal roda em teoria em todas as plataformas suportadas pelo Java, mas
algumas funcionalidades como o uso do speaker interno, manipulação do drive de
disco óptico e limpeza do console no cliente podem não estar disponíveis na
plataforma onde o Variable-Terminal esteja rodando. Tais funcionalidades
específicas estão em teoria disponíveis para as seguintes plataformas: Win32,
Linux, FreeBSD, OpenBSD, OpenSolaris e Darwin. O modo gráfico roda apenas se
houver algum ambiente gráfico no servidor e no cliente, como o protocolo X11 ou
o ambiente gráfico proprietário do Windows e precisa haver o suporte a tal
ambiente gráfico pela máquina virtual Java do cliente e do servidor.

Variable-Terminal é licenciado por GPL, veja o arquivo license.txt para detalhes.

###### Agradecimentos ######

Paulo Panhoto "Paulinho"
F. G. Testa "Testa"
Roberto Elvira "Fino"
Idalton Costa "TON"
por eventuais consultorias tecnológicas e conceituais.

Rodrigo Augusto do Nascimento Ribeiro "Mestre"
Fabrício Ferrari de Campos "Abigo"
Diogo M. Bispo "dbispo"
Renata C. B. Madeo "sweet_and_insane"
Rafael Yuji Morita "cable128"
Thais Souza "Tata"
William A. M. Gnann "pi/will/wgnann"
Arnon Eiji Furukawa "nihonjin"
Roberto Capelo "Slayer"
Robson Taru "Taru"
Andre Pantaleão "cabeça"
Felipe Waitman "wait"
Marianny Barcellos de Lima "mary"
Bruna Oliveira "bruna"
por terem sido alpha-testers/beta-testers.

###### Histórico de Versões ######

1.0.0: Lançamento Inicial.

###### Manual do Usuário ######

Para inicialização do Variable-Terminal, há os scripts:

* VTStart-Standard.bat: inicializa o Variable-Terminal no Windows, usando console
padrão, podendo escolher o modo.
* VTStart-Graphical.bat: inicializa o Variable-Terminal no Windows, usando
console gráfico, podendo escolher o modo.
* VTServer-Standard.bat: inicializa o servidor do Variable-Terminal no Windows, 
usando console padrão.
* VTServer-Graphical.bat: inicializa o servidor do Variable-Terminal no Windows,
usando console gráfico.
* VTDaemon-Standard.bat: inicializa o servidor do Variable-Terminal no Windows,
mas sem haver um console interativo no servidor. Pode-se configurar os
parâmetros de inicialização editando-se o arquivo batch.
* VTClient-Standard.bat: inicializa o cliente do Variable-Terminal no Windows,
usando console padrão.
* VTClient-Graphical.bat: inicializa o cliente do Variable-Terminal no Windows,
usando console gráfico.

* VTStart-Standard.sh: inicializa o Variable-Terminal no Linux, usando console
padrão, podendo escolher o modo.
* VTStart-Graphical.sh: inicializa o Variable-Terminal no Linux, usando console
gráfico, podendo escolher o modo.
* VTServer-Standard.sh: inicializa o servidor do Variable-Terminal no Linux,
usando console padrão.
* VTServer-Graphical.sh: inicializa o servidor do Variable-Terminal no Linux,
usando console gráfico .
* VTDaemon-Standard.sh: inicializa o servidor do Variable-Terminal no Linux, mas
como processo em segundo plano. Pode-se configurar os parâmetros de
inicialização editando-se o arquivo shellscript.
* VTClient-Standard.sh: inicializa o cliente do Variable-Terminal no Linux,
usando console padrão.
* VTClient-Graphical.sh: inicializa o cliente do Variable-Terminal no Linux,
usando console gráfico.

O console gráfico é um terminal de texto em modo gráfico feito em Java e esse
terminal costuma sem bem pior que os emuladores de terminal disponíveis nos
sistemas operacionais em geral.

Ao invés dos scripts, o Variable-Terminal pode ser inicializado clicando-se nos
arquivos "Variable-Terminal-*****.jar", mas nesse caso o Variable-Terminal é
inicializado usando console gráfico.

O arquivo "user-database.properties" é um arquivo de usuarios/senhas que são
utilizadas para realizar autenticação no servidor do Variable-Terminal. Cada linha
do arquivo é um par usuário/senha. Este arquivo "user-database.properties" deve
estar codificado em UTF-8.

O arquivo "server-settings.properties" é um arquivo de configuração do servidor
do Variable-Terminal, onde pode-se configurar alguns parâmetros do servidor do
Variable-Terminal. Este arquivo "server-settings.properties" deve estar codificado
em UTF-8.

Dependendo de como o Variable-Terminal for ser usado, pode ser necessário
ter a variável de ambiente VT_PATH configurada no sistema operacional, para
que o servidor do Variable-Terminal encontre os arquivos de usuários e de
configuração de segurança que ele deve carregar na inicialização. Essa variável
VT_PATH deve estar indicando o caminho no sistema de arquivos para a pasta do
aplicativo Variable-Terminal.

No caso do servidor do Variable-Terminal não encontrar um arquivo
"user-database.properties", se o servidor do Variable-Terminal estiver sendo
inicializado com alguma interatividade, pode-se configurar rapidamente apenas
uma conta de usuário para que se possa usar o servidor do Variable-Terminal.

No Variable-Terminal, "servidor" é sempre a máquina que será "controlada"
remotamente, e "cliente" é sempre a máquina que irá controlar o "servidor"
remotamente. Pode-se fazer com que o servidor do Variable-Terminal tente se
conectar com um cliente do Variable-Terminal, ao invés de fazer com que o
servidor do Variable-Terminal aguarde conexões de clientes do Variable-Terminal.
Essas opções são dadas na inicialização do servidor e do cliente do
Variable-Terminal, colocadas como "ativo" e "passivo". Um servidor "passivo"
aguarda conexão de cliente "ativo" e um cliente "passivo" aguarda conexão
de um servidor "ativo".

A parte de console do Variable-Terminal sempre é inicializada assim que algum
cliente do Variable-Terminal consegue se conectar e se autenticar num servidor
do Variable-Terminal. Essa parte de console depende quase que totalmente do
aplicativo "command.com" nos Windows 9X, do "cmd.exe" nos Windows mais recentes
e do "/bin/sh" no caso dos sistemas operacionais baseados em Unix. Sem acesso
a esses aplicativos de shell, a parte de console não funciona direito. Na parte
de console de Variable-Terminal, não há suporte para praticamente quase nenhum dos
recursos de emuladores de terminal virtual. Há apenas o básico de linha de
comando. Em compensação, o console do Variable-Terminal possui alguns comandos
próprios, que quando são detectados pelo servidor do Variable-Terminal, são
tratados diretamente pelo Variable-Terminal e não são passados para os aplicativos
do shell nativo do sistema operacional onde o Variable-Terminal está rodando.

Entre os comandos próprios do console do Variable-Terminal no lado cliente:

*VTTIME: Mostra a data/hora no servidor onde o cliente está conectado.

*VTCLEAR: Limpa o a tela do console do cliente do Variable-Terminal.

*VTDISCONNECT: Desconecta o cliente do servidor, permitindo que o cliente
tente conectar em algum outro servidor.

*VTQUIT: Finaliza o cliente.

*VTTERMINATE: Finaliza o servidor onde o cliente está conectado.

*VTBELL: Toca o sinal de beep padrão do console do sistema operacional do
servidor onde o cliente está conectado.

*VTFILEROOTS: Lista as raízes do sistema de arquivo no servidor
onde o cliente está conectado.

*VTPRINTSERVICES: Lista os serviços de impressão disponíveis no servidor
onde o cliente está conectado.

*VTNETWORKINTERFACES: Lista as interfaces de rede disponíveis no servidor
onde o cliente está conectado e apresenta informações disponíveis sobre cada
uma delas.

*VTDISPLAYDEVICES: Lista os dispositivos gráficos disponíveis no
servidor onde o cliente está conectado e apresenta informações disponíveis
sobre cada um deles.

*VTSETTINGSLIST: Lista os valores atuais dos parâmetros configuráveis do
servidor onde o cliente está conectado.

*VTSESSIONSLIST: Lista informações de conexões estabelecidas no servidor
onde o cliente está conectado. Apenas as conexões do Variable-Terminal, não todas
as conexões de rede da máquina onde está rodando o servidor.

*VTDETECTCHAINS: Detecta sessões do Variable-Terminal encadeadas, sendo que,
a cada sessão encadeada detectada será exibida uma mensagem.

*VTRESTARTSHELL: Tenta reiniciar o shell nativo no servidor onde o cliente
está conectado.

*VTSTOPSHELL: Fecha o shell nativo no servidor onde o cliente está conectado.

*VTAUDIOLINK: Inicia ou para a funcionalidade de comunicação via audio entre
cliente e servidor.

*VTHELP [COMMAND]: Dá detalhes de um determinado comando ou lista os comandos
suportados pelo console do cliente do Variable-Terminal e explica brevemente a
sintaxe de cada um.

*VTMESSAGE MESSAGE: Envia uma mensagem para o servidor onde o cliente está
conectado. "MESSAGE" pode ser qualquer coisa.

*VTNVIRONMENT [NAME] [VALUE]: Mostra/configura uma variável de ambiente
específica no servidor onde o cliente está conectado. "NAME" deve ser o
nome da variável de ambiente a ser procurada. Se "VALUE" estiver sendo usado,
a variável de ambiente procurada assume o valor passado em "VALUE".

*VTPROPERTY [NAME] [VALUE]: Mostra uma propriedade de JVM específica no
servidor onde o cliente está conectado. "NAME" deve ser o nome da
propriedade de JVM a ser procurada. Se "VALUE" estiver sendo usado, a
propriedade de JVM procurada assume o valor passado em "VALUE".

*VTSESSIONSLIMIT [LIMIT]: Configura ou mostra o limite de sessões no servidor
onde o cliente está conectado.

*VTCONNECTIONMODE [MODE]: Configura ou mostra o modo de conexão no servidor
onde o cliente está conectado.

*VTCONNECTIONHOST [HOST]: Configura ou mostra o host para conexão no servidor
onde o cliente está conectado.

*VTCONNECTIOPORT [PORT]: Configura ou mostra a porta para conexão no servidor
onde o cliente está conectado.

*VTPROXYTYPE [TYPE]: Configura ou mostra o tipo de proxy para conexão no
servidor onde o cliente está conectado. "TYPE" pode ser desabilitado (D),
Socks (S) ou HTTP (H).

*VTPROXYHOST [HOST]: Configura ou mostra o host do proxy no servidor onde o
cliente está conectado.

*VTPROXYPORT [PORT]: Configura ou mostra a porta do proxy no servidor onde o
cliente está conectado.

*VTPROXYSECURITY [STATE]: Configura ou mostra se a segurança de proxy 
habilitada ou desabilitada no servidor onde o cliente está conectado.
"STATE" pode ser desabilitado (D) ou habilitado (E).

*VTPROXYUSER [USER]: Configura ou mostra o usuário de proxy no servidor onde o
cliente está conectado.

*VTPROXYPASSWORD [PASSWORD]: Configura ou mostra a senha de proxy no servidor
onde o cliente está conectado.

*VTNCRYPTIONTYPE [TYPE]: Configura ou mostra o tipo de encriptação no
servidor onde o cliente está conectado. "TYPE" pode ser desabilitado (D),
RC4 (R) ou AES (A).

*VTNCRYPTIONPASSWORD [PASSWORD]: Configura ou mostra a chave de encriptação
no servidor onde o cliente está conectado.

*VTLOCALWORKDIRECTORY [PATH]: Configura ou mostra o diretório de trabalho
de comandos próprios do Variable-Terminal envolvendo arquivos no cliente.

*VTREMOTEWORKDIRECTORY [PATH]: Configura ou mostra o diretório de trabalho
de comandos próprios do Variable-Terminal envolvendo arquivos no servidor.

*VTFILEINFO PATH: Mostra informações sobre um arquivo ou pasta no sistema de
arquivos do servidor. "PATH" deve ser o caminho para esse arquivo ou pasta no
sistema de arquivos do servidor.

*VTFILESLIST PATH: Mostra informações sobre o conteúdo de uma pasta no sistema
de arquivos do servidor. "PATH" deve ser o caminho para essa pasta no sistema
de arquivos do servidor.

*VTFILECREATE PATH: Cria um arquivo vazio no sistema de arquivos do servidor.
"PATH" deve ser o caminho para esse arquivo no sistema de arquivos do servidor.

*VTFOLDERCREATE PATH: Cria uma pasta vazia no sistema de aquivos do
servidor. "PATH" deve ser o caminho para essa pasta no sistema de arquivos do
servidor.

*VTFILEDELETE PATH: Apaga um arquivo ou pasta no sistema de arquivos do
servidor. "PATH" deve ser o caminho para esse arquivo ou pasta no sistema de
arquivos do servidor.

*VTHOSTRESOLVE HOST: Usa resolução de hosts em rede no servidor onde o cliente
está conectado e apresenta os resultados encontrados na rede. "HOST" deve ser o
nome que será procurado pela rede no servidor.

*VTPRINTTEXT TEXT [PRINTER]: Imprime texto em uma impressora disponivel no
servidor. "TEXT" deve ser o texto a ser impresso. "PRINTER" é o número da
impressora a ser usada no servidor, caso não seja utilizado a impressora
padrão do servidor é que imprimirá o texto.

*VTPRINTFILE FILE [PRINTER]: Imprime um arquivo em uma impressora disponivel no
servidor. "TEXT" deve ser o arquivo a ser impresso. "PRINTER" é o número da
impressora a ser usada no servidor, caso não seja utilizado a impressora
padrão do servidor é que imprimirá o arquivo.

*VTREMOTEGRAPHICSMODE [MODE]: Inicia/Pára o modo gráfico do Variable-Terminal.
Também pode-se parar o modo gráfico fechando-se a janela de modo gráfico que
surge no cliente quando o modo gráfico é iniciado. Se "MODE" for (C), inicia
o modo gráfico com suporte a envio de comandos para o ambiente gráfico remoto,
se for (V), inicia o modo gráfico apenas capturando tela (sem enviar eventos de
teclado e mouse), e se for (S) pára o modo gráfico.

*VTBROWSE URI: Abre determinada URI no browser padrão no servidor. O parâmetro
URI deve ser uma URI válida.

*VTGRAPHICSALERT ALERT [TITLE]: Cria um alerta na interface gráfica do
servidor. O parâmetro "ALERT" deve ser a mensagem a ser exibida, o parâmetro
"TITLE" é opcional e representa o título da janela de mensagem e o parâmetro
"DISPLAY" pode ser um dispositivo gráfico válido para exibir a mensagem.

*VTOPTICALDRIVE COMMAND: Abre/Fecha o drive de disco óptico do servidor onde o
cliente está conectado. Se "COMMAND" for (O), abre o drive de disco óptico,
se for (C), fecha o drive de disco óptico.

*VTSCREENSHOT TYPE QUALITY [DISPLAY]: Captura a tela no servidor onde o cliente
está conectado e salva em um arquivo ".png" no diretório onde o Variable-Terminal
está sendo executado. Se "TYPE" for (C), a captura de tela não inclui uma
representação da posição do cursor do mouse e se for (S) a captura de tela
inclui uma representação da posição atual do cursor do mouse.
O parâmetro "QUALITY" determina a paleta de cores usada na captura de tela,
sendo que se "QUALITY" for (L) usa-se apenas 216 cores, se for (M) se
tem 32768 cores e se for (H) usa-se 24 bits de cores com 16777216 cores.
O parâmetro "DISPLAY" é opcional e caso seja usado deve ser um número de
dispositivo gráfico disponível no servidor.

*VTFILEMOVE [SOURCE DESTINATION]: Move e/ou renomeia um arquivo ou pasta no
sistema de arquivos no servidor. "SOURCE" deve ser o arquivo ou pasta a ser
movido/renomeado no sistema de arquivos do servidor. "DESTINATION" deve ser
o caminho para onde o arquivo será movido/renomeado. Pode-se usar apenas
"SOURCE" como (S) para interromper a operação de movimentação de arquivo.

*VTFILECOPY [SOURCE DESTINATION]: Copia um arquivo no sistema de arquivos no
servidor. "SOURCE" deve ser o arquivo a ser copiado no sistema de arquivos do
servidor. "DESTINATION" deve ser o caminho para onde o arquivo será copiado.
Pode-se usar apenas "SOURCE" como (S) para interromper a operação de cópia.

*VTZIPCREATE SIDE [ZIPFILE SOURCES]: Cria uma arquivo do tipo "zip" no
sistema de arquivos do servidor. "ZIPFILE" deve ser o caminho desejado para
a criação do arquivo "zip". "SOURCES" deve ser uma lista separada por ponto e
vírgula de arquivos/pastas que devem ser inclusos no arquivo "zip" a ser
criado. Pode-se usar apenas "ZIPFILE" como (S) para interromper a
operação de criação de arquivo "zip", omitindo-se o parâmetro "SOURCES".

*VTZIPSTORE SIDE [ZIPFILE SOURCES]: Cria uma arquivo do tipo "zip" sem
compressão no sistema de arquivos do servidor. "ZIPFILE" deve ser o caminho
desejado para a criação do arquivo "zip" sem compressão. "SOURCES" deve ser uma
lista separada por ponto e vírgula de arquivos/pastas que devem ser inclusos no
arquivo "zip" sem compressão a ser criado. Pode-se usar apenas "ZIPFILE"
como (S) para interromper a operação de criação de arquivo "zip" sem compressão
, omitindo-se o parâmetro "SOURCES".

*VTZIPEXTRACT SIDE [ZIPFILE DESTINATION]: Extrai o conteúdo de um arquivo
do tipo "zip" para um caminho no sistema de arquivos do servidor. "ZIPFILE"
deve ser o caminho no sistema de arquivo para o arquivo do tipo "zip" cujo
conteúdo será extraído. "DESTINATION" deve ser o caminho no sistema de arquivos
onde o conteúdo do arquivo do tipo "zip" será extraído. Pode-se usar apenas
"ZIPFILE" como (S) para interromper a operação de extração de arquivo
"zip", omitindo-se o parâmetro "DESTINATION".

*VTTCPTUNNELS [SIDE SOURCE] [TARGET]: Cria e gerencia túneis TCP de
redirecionamento para conexão entre a rede do cliente e a rede do servidor.
Sem nenhum parâmetro, exibe os túneis configurados atualmente. O parâmetro SIDE
indica se a porta de escuta será local(L) ou remota(R); O parâmetro "SOURCE" é
o número da porta de rede de escuta do túnel. O parâmetro "TARGET" é o endereço
de rede para onde será feito o redirecionamento da conexão recebida na porta de
escuta configurada, e sem esse parâmetro o túnel que usa a porta de escuta
configurada pelo parâmetro "SOURCE" é excluído.

*VTRUNTIME COMMAND [PARAMETERS]: Cria e gerencia processos nativos no servidor
onde o cliente está conectado e provê alguns modos de gerenciá-los. "COMMAND"
tem diversas opções "" e "PARAMETERS" deve ser o caminho completo do
aplicativo a ser inicializado.

*VTBEEP FREQUENCY TIME: Toca sons no servidor onde o cliente está
conectado. No Linux, depende de se ter acesso ao arquivo "/dev/console".
"FREQUENCY" deve ser a frequência em Hertz do beep, e "TIME" deve ser a
duração do beep em milissegundos.

*VTFILETRANSFER [MODE] [SOURCE DESTINATION]: Faz transferência de arquivos entre o
cliente e o servidor onde o cliente está conectado. Se "MODE" for (G), faz
download de arquivos do servidor para o cliente, se for (P), faz upload
de arquivos do cliente para o servidor. "SOURCE" deve ser sempre um caminho
COMPLETO de um ARQUIVO de origem, PASTAS NÃO SÃO ACEITAS. No caso de download,
"SOURCE" deve ser um caminho COMPLETO para o arquivo no servidor, no cado de
upload, deve ser um caminho COMPLETO para o arquivo no cliente. "DESTINATION"
deve ser sempre um caminho COMPLETO de um ARQUIVO de destino, PASTAS NÃO SÃO
ACEITAS. No caso de upload, "DESTINATION" deve ser um caminho COMPLETO para o
arquivo no servidor, no cado de download, deve ser um caminho COMPLETO para
o arquivo no cliente. Relembrando, PASTAS NÃO SÃO ACEITAS. É possível
interromper transferências de arquivo usando-se "MODE" como (S), sem passar-se
outros parâmetros.

O modo gráfico é opcional, só sendo ativado quando pedido pelo usuário,
procurando diminuir o uso de CPU e largura de banda de rede. O modo gráfico
tem a opção de usar apenas 216 cores, também visando diminuir o gasto com
CPU e largura de banda de rede do modo gráfico quando este está ativado. Há
planos para se o otimizar o quanto for possível o uso de largura de banda
usando-se técnicas de transferência de tela melhores nas próximas versões.

Atenção: nos comandos do Variable-Terminal que usam argumentos que são caminhos no
sistema de arquivos, recomenda-se que sejam passados sempre caminhos absolutos
no sistema de arquivos. No caso de se passar caminhos relativos nos argumentos,
o caminho no sistema de arquivos que é resolvido é relativo à pasta onde o
servidor ou o cliente do Variable-Terminal foram inicializados, ou então o caminho
resolvido é relativo ao configurado nos comandos "*VTLOCALWORKDIRECTORY" e
"*VTREMOTEWORKDIRECTORY". Os comandos próprios do Variable-Terminal não
conseguem saber em qual contexto de diretório o shell nativo está.

Há alguns poucos comandos que podem ser usados no console do servidor do
Variable-Terminal:

*VTTIME: Mostra a data/hora no servidor.

*VTCLEAR: Limpa o a tela do console do servidor do Variable-Terminal.

*VTTERMINATE: Finaliza o servidor.

*VTFILEROOTS: Lista as raízes do sistema de arquivo no servidor.

*VTPRINTSERVICES: Lista os serviços de impressão disponíveis no servidor.

*VTNETWORKINTERFACES: Lista as interfaces de rede disponíveis no servidor
e apresenta informações disponíveis sobre cada uma delas.

*VTDISPLAYDEVICES: Lista os dispositivos gráficos disponíveis no
servidor e apresenta informações disponíveis sobre cada um deles.

*VTSETTINGSLIST: Lista os valores atuais dos parâmetros configuráveis do
servidor.

*VTSESSIONSLIST: Lista informações de conexões estabelecidas no servidor.
Apenas as conexões do Variable-Terminal, não todas as conexões de rede da máquina
onde está rodando o servidor.

*VTHELP [COMMAND]: Dá detalhes de um determinado comando ou lista os comandos
suportados pelo console do servidor do Variable-Terminal e explica brevemente a
sintaxe de cada um.

*VTMESSAGE MESSAGE: Envia uma mensagem para todos os clientes que estão
conectados no servidor. "MESSAGE" pode ser qualquer coisa.

*VTDISCONNECT [CLIENT]: Desconecta um cliente específico do servidor ou
todos os clientes conectados no servidor.

*VTNVIRONMENT [NAME] [VALUE]: Mostra/configura uma variável de ambiente
específica no servidor onde o cliente está conectado. "NAME" deve ser o
nome da variável de ambiente a ser procurada. Se "VALUE" estiver sendo usado,
a variável de ambiente procurada assume o valor passado em "VALUE".

*VTPROPERTY [NAME] [VALUE]: Mostra uma propriedade de JVM específica no
servidor onde o cliente está conectado. "NAME" deve ser o nome da
propriedade de JVM a ser procurada. Se "VALUE" estiver sendo usado, a
propriedade de JVM procurada assume o valor passado em "VALUE".

*VTSESSIONSLIMIT [LIMIT]: Configura ou mostra o limite de sessões no servidor.

*VTCONNECTIONMODE [MODE]: Configura ou mostra o modo de conexão no servidor.

*VTCONNECTIONHOST [HOST]: Configura ou mostra o host para conexão no servidor.

*VTCONNECTIONPORT [PORT]: Configura ou mostra a porta para conexão no servidor.

*VTPROXYTYPE [TYPE]: Configura ou mostra o tipo de proxy para conexão no
servidor. "TYPE" pode ser desabilitado (D), Socks (S) ou HTTP (H).

*VTPROXYHOST [HOST]: Configura ou mostra o host do proxy no servidor.

*VTPROXYPORT [PORT]: Configura ou mostra a porta do proxy no servidor.

*VTPROXYSECURITY [STATE]: Configura ou mostra se a segurança de proxy 
habilitada ou desabilitada no servidor. "STATE" pode ser desabilitado (D) ou
habilitado (E).

*VTPROXYUSER [USER]: Configura ou mostra o usuário de proxy no servidor.

*VTPROXYPASSWORD [PASSWORD]: Configura ou mostra a senha de proxy no servidor.

*VTNCRYPTIONTYPE [TYPE]: Configura ou mostra o tipo de encriptação no
servidor. "TYPE" pode ser desabilitado (D), RC4 (R) ou AES (A).

*VTNCRYPTIONPASSWORD [PASSWORD]: Configura ou mostra a chave de encriptação
no servidor.

###### Código Fonte ######

O código fonte do Variable-Terminal em si encontra-se na pasta src do projeto do
Variable-Terminal. A pasta do projeto do Variable-Terminal é uma pasta de projeto do
Eclipse 3.4. O Variable-Terminal começou a ser desenvolvido quando as noções de
orientação a objeto do seu desenvolvedor ainda não estavam muito sólidas.

O arquivo "build.xml" incluso no código fonte é um script do Ant capaz de
construir o projeto do Variable-Terminal tanto como biblioteca compartilhada como
aplicativo.

Algumas idéias e conceitos usados, foram obtidos do código fonte do software
Poor Woman's Telnet Server (PWTS), ainda que na época o desenvolvedor do
Variable-Terminal se sentisse meio confuso ao tentar entender esse software apenas
estudando o código-fonte do mesmo.
Link para o PWTS: http://pwts.sourceforge.net/

###### Perguntas Frequentes ######

P: Por que o nome Variable-Terminal?
R: É uma referência a duas coisas, o fato do apelido do desenvolvedor do
software ter sido 'SaTaN' e também a um software de administração remota
comercial já existente no mercado.

P: Afinal, qual a língua do Variable-Terminal? Inglês ou português?
R: O software Variable-Terminal em si funciona todo em inglês, pelo menos em
teoria. O manual do Variable-Terminal permanece em português-brasil devido à falta
no desenvolvedor de conhecimentos suficientes sobre o inglês necessários para
elaboração de uma tradução para o inglês decente do manual atual. Ainda que
pareça óbvio para alguns, vale a pena informar que o desenvolvedor do
Variable-Terminal é um brasileiro.

P: O desenvolvedor do Variable-Terminal é um adorador do diabo/demônio?
R: O desenvolvedor do Variable-Terminal acredita que não. Se nomear um software
com o nome "Variable-Terminal" faz do desenvolvedor do mesmo um adorador do diabo,
talvez caiba ao usuário final decidir, como também deve caber ao usuário julgar
se tal fato o impede de usar um software com este nome. É no que acredita o
desenvolvedor do Variable-Terminal.

P: Por que o Variable-Terminal não tem uma interface com maior usabilidade?
R: O foco do Variable-Terminal não é, e jamais foi usabilidade. Ao invés disso, o
Variable-Terminal segue um modelo de desenvolvimento mais minimalista, priorizando
performance e economia de recursos.

P: Por que o Variable-Terminal possui comandos próprios tão grandes?
R: O desenvolvedor do Variable-Terminal pensa que assim diminuem-se as chances de
que haja algum programa ou comando do próprio shell nativo que possua o mesmo
nome de um comando próprio do Variable-Terminal. Há também uma forma abreviada
para os comandos considerados grandes.

###### Contato ######

Sugestões/Notificações/Relatos de bugs podem (ou não) ser bem-vindos.

Pede-se que haja o nome "SATAN_ANYWHERE" no assunto (ou "subject") dos
emails enviados.

O desenvolvedor do Variable-Terminal não dá garantia alguma sobre se irá ou
não responder os emails enviados, nem se atenderá a pedidos que lhe forem
enviados por email.

Email de contato do desenvolvedor: wknishio@gmail.com