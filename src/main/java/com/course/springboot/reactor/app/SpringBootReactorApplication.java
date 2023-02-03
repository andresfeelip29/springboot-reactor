package com.course.springboot.reactor.app;

import com.course.springboot.reactor.app.Entities.CommentEntity;
import com.course.springboot.reactor.app.Entities.UserCommentEntity;
import com.course.springboot.reactor.app.Entities.UserEntity;
import org.apache.commons.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {


    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //exapleIterables();
        //exampleOperatorBasic();
        //exapleFlatMap();
        //convertToFluxToString();
        //convertListToOneMonoList();
        //ExampleUserCommentFlatMap();
        //ExampleUserCommentZipWith();
        //ExampleUserCommentZipWithForm2();
        //ExampleZipWithAndRange();
        //ExampleTimeIntervalDelay();
        //ExampleDelayElements();
        //ExampleInfinityInterval();
        ExampleCreateIntervaObservable();

    }

    //------------------------------------------- Example create a flow from cero, using .complete() for finish de flow, doOnComplete for emitter response to complete de flow ------------------------------------------------------

    public void ExampleCreateIntervaObservable() {

        Flux.create(emmitter -> {
                    Timer time = new Timer();
                    time.schedule(new TimerTask() {
                        private Integer count = 0;

                        @Override
                        public void run() {
                            emmitter.next(++count);
                            if (count == 10){
                                time.cancel();
                                emmitter.complete();
                            }

                            if (count == 5){
                                time.cancel();
                                emmitter.error(new InterruptedException("Counter equals 5"));
                            }
                        }
                    }, 1000, 1000);
                })
                .doOnComplete(() -> log.info("Task finished"))
                .subscribe(next -> log.info(next.toString()), err -> log.error(err.getMessage()));

    }

    //------------------------------------------- Example using Flux interval to issue an infinite flow of integers, CountDownLatch for count int concurrent, doOnTerminate execute task to end flow and retry() for executed n time the flow to end------------------------------------------------------

    public void ExampleInfinityInterval() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown)
                .flatMap(x -> {
                    if (x >= 5) {
                        return Flux.error(new InterruptedException("Max range to 5"));
                    }
                    return Flux.just(x);
                })
                .map(x -> "Hola " + x)
                .retry(2)
                .subscribe(text -> log.info(text), err -> log.error(err.getMessage()));

        latch.await();
    }

    //------------------------------------------- Example using DelayElements, and block Thread using Thread.sleep()------------------------------------------------------

    public void ExampleDelayElements() throws InterruptedException {

        Flux<Integer> range = Flux.range(1, 12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(interval -> log.info(interval.toString()));

        range.subscribe();
        Thread.sleep(13000);

    }

    //------------------------------------------- Example using zipWith and Delay in the flow, and block Thread using blockLast()------------------------------------------------------

    public void ExampleTimeIntervalDelay() {

        Flux<Integer> range = Flux.range(1, 12);

        Flux<Long> delay = Flux.interval(Duration.ofSeconds(1));

        range.zipWith(delay, (ra, de) -> ra)
                .doOnNext(interval -> log.info(interval.toString()))
                .blockLast();

    }

    //------------------------------------------- Example using zipWith and Range------------------------------------------------------

    public void ExampleZipWithAndRange() {

        Flux.just(1, 2, 3, 4, 5)
                .map(x -> (x * 2))
                .zipWith(Flux.range(0, 5), (stream1, stream2) -> String.format("First Flux %d, Second Flux %d", stream1, stream2))
                .subscribe(text -> log.info(text));

    }

    //------------------------------------------- Example transform two flow in one flow using zipWith Form2-------------------------------------------------------

    public void ExampleUserCommentZipWithForm2() {
        Mono<UserEntity> userEntityMono = Mono.fromCallable(() -> createUser());

        Mono<CommentEntity> commentEntityMono = Mono.fromCallable(() -> {
            CommentEntity comments = new CommentEntity();
            comments.addComments("Hi Jonh, how are you?");
            comments.addComments("Yor are a programmer?");
            comments.addComments("Yes, I used Java with reactor now");
            return comments;
        });

        Mono<UserCommentEntity> userCommentEntityMono = userEntityMono
                .zipWith(commentEntityMono)
                .map(tuple -> {
                    UserEntity u = tuple.getT1();
                    CommentEntity c = tuple.getT2();
                    return new UserCommentEntity(u, c);
                });

        userCommentEntityMono.subscribe(uc -> log.info(uc.toString()));
    }

    //------------------------------------------- Example transform two flow in one flow using zipWith-------------------------------------------------------
    public UserEntity createOtherUser() {
        return new UserEntity("Richard", "Gray");
    }

    public void ExampleUserCommentZipWith() {
        Mono<UserEntity> userEntityMono = Mono.fromCallable(() -> createUser());

        Mono<CommentEntity> commentEntityMono = Mono.fromCallable(() -> {
            CommentEntity comments = new CommentEntity();
            comments.addComments("Hi Jonh, how are you?");
            comments.addComments("Yor are a programmer?");
            comments.addComments("Yes, I used Java with reactor now");
            return comments;
        });

        Mono<UserCommentEntity> userCommentEntityMono = userEntityMono
                .zipWith(commentEntityMono, (u, c) -> new UserCommentEntity(u, c));

        userCommentEntityMono.subscribe(uc -> log.info(uc.toString()));
    }

    //------------------------------------------- Example transform two flow in one flow using flatMap-------------------------------------------------------
    public UserEntity createUser() {
        return new UserEntity("Jonh", "Doe");
    }

    public void ExampleUserCommentFlatMap() {
        Mono<UserEntity> userEntityMono = Mono.fromCallable(() -> createUser());

        Mono<CommentEntity> commentEntityMono = Mono.fromCallable(() -> {
            CommentEntity comments = new CommentEntity();
            comments.addComments("Hi Jonh, how are you?");
            comments.addComments("Yor are a programmer?");
            comments.addComments("Yes, I used Java with reactor now");
            return comments;
        });
        userEntityMono.flatMap(u -> commentEntityMono.map(c -> new UserCommentEntity(u, c))).subscribe(uc -> log.info(uc.toString()));
    }

    //------------------------------------------- Example convert List to Mono-------------------------------------------------------
    public void convertListToOneMonoList() {

        List<UserEntity> usersList = new ArrayList<>();
        usersList.add(new UserEntity("Scarlett", "biller"));
        usersList.add(new UserEntity("Andrew", "Facade"));
        usersList.add(new UserEntity("Jonh", "Doe"));
        usersList.add(new UserEntity("Emma", "Clauw"));
        usersList.add(new UserEntity("Andrew", "Endersen"));

        Flux.fromIterable(usersList)
                .collectList()
                .subscribe(rta -> log.info(rta.toString()));

    }

    //------------------------------------------- Example convert Flux to String-------------------------------------------------------
    public void convertToFluxToString() {

        List<UserEntity> usersList = new ArrayList<>();
        usersList.add(new UserEntity("Scarlett", "biller"));
        usersList.add(new UserEntity("Andrew", "Facade"));
        usersList.add(new UserEntity("Jonh", "Doe"));
        usersList.add(new UserEntity("Emma", "Clauw"));
        usersList.add(new UserEntity("Andrew", "Endersen"));

        Flux.fromIterable(usersList)
                .map(user -> user.getName().concat(" ").concat(user.getLastName()))
                .flatMap(name -> {
                    if (name.contains("Andrew")) {
                        return Mono.just(name);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(name -> {
                    return name.toUpperCase();
                }).
                subscribe(rta -> log.info(rta));

    }

    //------------------------------------------- Example using faltMap-------------------------------------------------------
    public void exapleFlatMap() {

        //Add flow data(publicer or observable) using Flux, the Observable is a inmutable stream
        Flux<String> names = Flux.just("Scarlett biller", "Andrew Facade", "Jonh Doe", "Emma Clauw", "Andrew Endersen");


        names.map(name -> new UserEntity(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
                .flatMap(user -> {
                    if (user.getName().equalsIgnoreCase("Andrew")) {
                        return Mono.just(user);
                    } else {
                        return Mono.empty();
                    }
                })
                .map(user -> {
                    String name = user.getName().toLowerCase();
                    user.setName(name);
                    return user;
                }).
                subscribe(rta -> log.info(rta.toString()));

    }

    //------------------------------------------- Example convert List to Flux-------------------------------------------------------
    public void exapleIterables() {

        //We can create a flow Observable from another iterable for example: List, Set, ArrayList, stream

        List<String> usersList = new ArrayList<>();
        usersList.add("Scarlett biller");
        usersList.add("Andrew Facade");
        usersList.add("Jonh Doe");
        usersList.add("Emma Clauw");
        usersList.add("Andrew Endersen");

        Flux<String> data = Flux.fromIterable(usersList);

    }

    //------------------------------------------- Example basic Operators-------------------------------------------------------
    public void exampleOperatorBasic() {

        //Add flow data(publicer or observable) using Flux, the Observable is a inmutable stream
        Flux<String> names = Flux.just("Scarlett biller", "Andrew Facade", "Jonh Doe", "Emma Clauw", "Andrew Endersen");

        Flux<UserEntity> users = names
                .map(name -> new UserEntity(name.split(" ")[0].toUpperCase(), name.split(" ")[1].toUpperCase()))
                .filter(user -> user.getName().equalsIgnoreCase("Andrew"))
                .doOnNext(user -> {
                            if (user == null) {
                                throw new RuntimeException("El nombre no puede ser vacio");
                            }
                            System.out.println(user.toString());

                        }
                ).map(user -> {
                    String name = user.getName().toLowerCase();
                    user.setName(name);
                    return user;
                }); //Operator map() used for transform the flw data type to other flow data any type

        //Used to subscribe to a data stream or observe the flow to perform a task.
        users.subscribe(rta -> log.info(rta.getName()), err -> log.error(err.getMessage()), () -> log.info("Flujo terminado"));
    }
}
