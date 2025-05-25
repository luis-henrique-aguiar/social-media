# 📱 Social Media

O projeto é um aplicativo desenvolvido para a disciplina de Dispositivos Móveis II (DMO II) do IFSP. Este projeto consiste em uma micro rede social que permite o compartilhamento de fotos, textos e localização entre usuários conectados. Com recursos de autenticação, postagens personalizadas e interações em tempo real, o app busca criar uma experiência dinâmica e envolvente para os usuários.

---

## ✨ Funcionalidades

### 🔹 Autenticação e Cadastro de Usuário  
Permite que novos usuários se cadastrem e façam login no aplicativo utilizando e-mail e senha, com autenticação via Firebase.  

### 🔹 Criação de Postagens  
Os usuários podem criar postagens contendo uma imagem (carregada da galeria), um texto descritivo e a cidade atual (obtida automaticamente via GPS).  

### 🔹 Feed de Postagens  
As postagens são carregadas a partir do Firebase Firestore, com paginação (5 por vez), e podem ser filtradas pelo nome da cidade.  

### 🔹 Perfil do Usuário  
Os usuários podem editar seus próprios dados, como nome, senha e foto, diretamente na tela de perfil.  

### 🔹 Geocodificação  
Ao criar uma postagem, o app obtém a localização atual e traduz as coordenadas em um nome de cidade.  

---

## 📌 Especificações do Projeto

- **📱 IDE:** Android Studio  
- **⚡ Linguagem:** Kotlin  
- **📲 Min API:** 28 (Android 9 Pie)  
- **📂 Banco de Dados:** Firebase (Authentication e Firestore)  
- **📍 Biblioteca de Localização:** Google Play Services  

---

# 🚀 Instalação do Aplicativo

Para utilizar o a **Micro Rede Social**, siga os passos abaixo:

## 1️⃣ Download do APK  
Baixe o APK diretamente no link abaixo:  

🔗 **[Download do APK](https://github.com/luis-henrique-aguiar/social-media/blob/main/release/social-media.apk)**  

## 2️⃣ Instalação no Android  
1. No seu dispositivo, abra **Configurações > Segurança** e ative a opção **Fontes desconhecidas** (caso o Android solicite permissão para instalar aplicativos externos).  
2. Localize o arquivo APK baixado no seu gerenciador de arquivos.  
3. Toque no APK para iniciar a instalação.  
4. Aguarde a instalação ser concluída e abra o aplicativo!  

Agora você pode começar a compartilhar momentos e interagir com outros usuários! 🚀📱  

---

## 👨‍💻 Desenvolvedores  

| Nome | RA |  
|-------|------|  
| Luis Henrique Aguiar dos Santos | AQ302234X |  
