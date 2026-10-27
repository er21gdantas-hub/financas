# Minhas Finanças (Android, Kotlin + Jetpack Compose)

App de finanças pessoais offline-first. Dados 100% locais (Room/SQLite). Sem permissão de internet.

## Abrir e rodar
1. Instale o Android Studio Koala (2024.1.1) ou mais novo.
2. File > Open e escolha a pasta MinhasFinancas (descompactada).
3. Aguarde o Gradle sincronizar (precisa de internet só nessa etapa).
4. Conecte o celular (Opções do desenvolvedor > Depuração USB) ou crie um emulador e clique em Run.

## Rodar os testes
No Android Studio: clique com o botão direito em app/src/test e escolha "Run Tests". São testes de JVM (rápidos, sem emulador): relatório, parcelas, orçamento, backup e formatação.

## APK para instalar no seu celular
Build > Build Bundle(s) / APK(s) > Build APK(s). O arquivo app-debug.apk fica em app/build/outputs/apk/debug/.
(Para instalar um APK, permita "instalar apps desconhecidos" para o app que o abrir.)

## AAB assinado (Play Store) ou APK de release
Opção A, pelo Android Studio: Build > Generate Signed App Bundle / APK, crie a chave e siga o assistente.
Opção B, automática: copie keystore.properties.example para keystore.properties, preencha com os dados da sua chave (.jks) e gere o release normalmente; o Gradle assina sozinho.
GUARDE o arquivo .jks e as senhas em lugar seguro (e fora do git): sem eles não dá para publicar atualizações do mesmo app.
Para a Play Store, o applicationId (br.com.minhasfinancas) precisa ser único e não muda depois. Troque em app/build.gradle.kts (applicationId e namespace) antes da primeira publicação, se quiser outro.

## Status
Fases 0 a 7 concluídas: dados, transações (parcelas e fixas), contas e transferência, categorias, orçamento com alerta, metas, relatórios com CSV e PDF, backup JSON, PIN e biometria, onboarding e testes.
