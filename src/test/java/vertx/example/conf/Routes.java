package vertx.example.conf;

import vertx.example.controllers.Application;
import ninja.Router;
import ninja.application.ApplicationRoutes;

public class Routes implements ApplicationRoutes {
    
    @Override
    public void init(Router router) {
        router.GET().route("/").with(Application.class, "home");
        router.GET().route("/test").with(Application.class, "test");
        router.GET().route("/parameters").with(Application.class, "parameters");
        router.POST().route("/parameters").with(Application.class, "parameters");
        router.POST().route("/upload1").with(Application.class, "upload1");
        router.POST().route("/upload2").with(Application.class, "upload2");
        router.POST().route("/benchmark_form").with(Application.class, "benchmark_form");
        router.POST().route("/benchmark_json").with(Application.class, "benchmark_json");
        router.POST().route("/basic_form").with(Application.class, "basic_form");
        router.GET().route("/scheme").with(Application.class, "scheme");
        router.GET().route("/remote_addr").with(Application.class, "remote_addr");
        router.GET().route("/request_path").with(Application.class, "request_path");
        router.GET().route("/param_parsers").with(Application.class, "param_parsers");
        router.POST().route("/param_parsers").with(Application.class, "param_parsers_post");
    }
    
}
