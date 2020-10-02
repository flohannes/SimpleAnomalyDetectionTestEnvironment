#docker build --no-cache -t flohannes/detector -f  Dockerfile .
FROM azul/zulu-openjdk:11.0.1
RUN apt-get update && apt-get install -y libopenblas-dev
COPY target/SimpleAnomalyDetectionTestEnvironment-*-jar-with-dependencies.jar /SimpleAnomalyDetectionTestEnvironment.jar
COPY --from=bitflowstream/bitflow-pipeline:static /bitflow-pipeline /

ENTRYPOINT ["/bitflow-pipeline", "-exe",\
            "java;java;-Duser.timezone=Europe/Berlin -jar /SimpleAnomalyDetectionTestEnvironment.jar -P iftm.service -P bitflow4j"]