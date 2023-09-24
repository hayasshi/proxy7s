# proxy7s

**This is toy project.**

色々な Java/Scala の HTTP ライブラリをつかって、Layer7 プロキシサーバーを書く。

ライブラリごとに、Socket から受信したデータをアプリケーションレイヤでどう扱っているかを知り、リソース効率よく処理するにはどうすればよいかを知るための、実験的なコード置き場。

## target

- AkkaHTTP 10.2 (Akka 2.6)
  - TCP の部分は AkkaStreams の Streaming TCP を利用しており、また Streaming TCP は Akka IO を利用している
  - Akka IO では、TCP ソケットからのデータを、Direct allocate した ByteBuffer に読み込み、読み込んだ分ずつ Actor メッセージとしてアプリケーションに送っている
    - https://github.com/akka/akka/blob/v2.6.21/akka-actor/src/main/scala/akka/io/TcpConnection.scala#L268
  - Streaming TCP では、それをストリームの要素として送っている
    - https://github.com/akka/akka/blob/v2.6.21/akka-stream/src/main/scala/akka/stream/scaladsl/Tcp.scala#L138
    - https://github.com/akka/akka/blob/v2.6.21/akka-stream/src/main/scala/akka/stream/impl/io/TcpStages.scala#L167
    - https://github.com/akka/akka/blob/v2.6.21/akka-stream/src/main/scala/akka/stream/impl/io/TcpStages.scala#L337-L341
  - AkkaHTTP の request parser 部分でも、結合せずに AkkaStreams の Source として提供している
    - https://github.com/akka/akka-http/blob/v10.2.10/akka-http-core/src/main/scala/akka/http/scaladsl/Http.scala#L239-L257
    - https://github.com/akka/akka-http/blob/v10.2.10/akka-http-core/src/main/scala/akka/http/scaladsl/Http.scala#L384-L389
    - https://github.com/akka/akka-http/blob/v10.2.10/akka-http-core/src/main/scala/akka/http/impl/engine/server/HttpServerBluePrint.scala#L230-L235
    - https://github.com/akka/akka-http/blob/v10.2.10/akka-http-core/src/main/scala/akka/http/impl/engine/parsing/HttpRequestParser.scala#L219
    - https://github.com/akka/akka-http/blob/v10.2.10/akka-http-core/src/main/scala/akka/http/impl/engine/parsing/HttpMessageParser.scala#L369-L375
  - これをそのまま proxy として接続先にリクエストすれば、受信側の TCP ソケットバッファから必要分のデータがアプリケーションを通り、送信側の TCP ソケット(バッファ)に直接送ることで、例えばリクエスト元が巨大なファイルを送信する場合でも、proxy アプリケーションのヒープメモリに巨大なデータを抱える必要がない
  - akka-http-client の実装をみていないので、client 側で結合している場合は、ヒープを確保する必要がある
- PekkoHTTP
  - AkkaHTTP と同様
- http4s
  - TODO
- ...

## feature

- HTTP リクエストをルーティングする
- Path や QueryParameter などでルーティング先を決定できる
- ...
