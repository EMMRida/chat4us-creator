# Chat4Us - Cross-Platform Chat Bots Creator

Chat4Us is a lightweight open-source cross-platform chat bots creator that anyone can use to design chat flows, from static Q/A to AI-driven discussions and when needed human chat escalation through the messenger app.
Chatbots can be created and designed using a visual interface in even the most basic to advanced configurations, utilizing scripting capabilities to address special scenarios such as local/remote data management and continuous discussion monitoring and moderation.
Chat flows can be connected either to local/offline LLMs using GPT4All or external APIs providers like OpenAI, DeepSeek or Groq.

Chat4Us contains two projects:
- Chat4Us-Creator: The core of the system, provides tools for creating, managing and administering chatbots, remote clients and human operators.
- Chat4Us-Agent: The messenger app that will continue the discussion with remote users when needed.

Project Depencies: Java (JRE) 21 or higher to be installed before launching apps. GPT4All when needing to use local/offline LLMs for AI-driven chats.

This project is developped using Eclipse IDE. Binaries were tested on Windows and Ubuntu (WSL). MacOS (not yet tested).

Encrypted SSL certificates are used to secure components communication. You should generate or import a certificate asap. Default password: a123456/* 

Help documents, tutorials, examples and sample software/web clients are available on [chat4usai.com](https://chat4usai.com).
