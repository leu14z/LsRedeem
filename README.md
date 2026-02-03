LsRedeem
Sistema profissional de resgate de códigos para servidores de Minecraft, desenvolvido para ser leve e seguro. 
O diferencial deste plugin é o uso de um banco de dados local (SQLite) em vez de arquivos de texto comuns, o que evita erros e lentidão no servidor.

Funcionalidades principais
O plugin permite criar códigos de recompensa com limites de uso. Ele possui um sistema de proteção que impede que um jogador use o mesmo código em várias contas diferentes através da verificação do endereço de IP.

/redeem [codigo] - Resgata a recompensa do código.
/lsredeem criar [codigo] [usos] - Cria um código (0 para usos ilimitados).
/lsredeem deletar - Deleta código que você criou.
/lsredeem list - Lista todos os códigos e quantos usos restam.
/lsredeem resetar [jogador] [codigo] - Permite que o jogador use o código novamente.
/lsredeem resetartudo [jogador] - Reseta todos os códigos de um jogador.
/lsredeem reload - Recarrega as configurações.

Comandos de Jogador
Permissão: LsRedeem.redeem

Comandos de Administração
Permissão: LsRedeem.admin

Variáveis (Placeholders)
Você pode usar estas variáveis nos comandos de recompensa dentro da config:

%player% - Nome do jogador.
%code% - O código usado.
%initial_uses% - Total de usos originais do código.

Developer: leu14z
GitHub: https://github.com/leu14z
