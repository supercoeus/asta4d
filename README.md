## What is Asta4D

It is a web application framework which is friendly to design and flexible to development.
It is inspired by lift which is a famous scala web application framework. Asta4D is therefore 
developed as an alternative of [lift](http://liftweb.net/) for Java. If you like scala, we strongly recommend lift 
for you and if you‘d like to enjoy the benefit of lift while still staying with Java, we believe 
our Asta4D is your best fit.


## Why Asta4D
In the past decade, plenty of Java based web application frameworks are generated. Especially the MVC 
architecture and JSP tag libs (or other traditional template technologies) that has greatly released our 
productivity. But unfortunately, we are still suffering from the following situations:

1. The designers or front-end engineers are keeping complaining the mixed-in dynamic code, as they disturb their efforts of redesigning the page style or structure. And in the mean time, the back-end developers are also complaining that the front-end guys break the working page frequently,  because redesign or the new design is hard to merge due to the huge cost of source refactoring. 
1. The developers are complaining about the poor functionalities of template language which they are using and tired from the various magic skills for complex rendering logic.
1. The developers are discontented with the counterproductivity of MVC architecture and desire a more efficient approach like traditional PHP/ASP development.

Thus, we created Asta4D.

## A taste of Asta4D
-   Separated template and rendering logic

    There is no dynamic code in template file. An Asta4D template file is always a pure HTML file which can be 
    easily maintained by front-end developers, it is very design friendly and we can reduce the workload for 
    source refactoring by over 90%.


    ```html
    <section>
        <article>
            <div afd:render="SimpleSnippet">dummy text</div>
            <afd:snippet render="SimpleSnippet:setProfile">
                <p id="name">name:<span>dummy name</span></p>
                <p id="age">age:<span>0</span></p>
            </afd:snippet>
        </article>
    </section>
    ```


    There is only some minimal mix-in extra declarations that tell template engine which Java class will rendering these 
    html contents, which Java class is usually called as a snippet class.

    In the snippet class, we use traditional CSS selector to reference rendering target, amazing and powerful.


    ```java
    public class SimpleSnippet {

        public Renderer render(String name) {
            if (StringUtils.isEmpty(name)) {
                name = "Asta4D";
            }
            Element element = ElementUtil.parseAsSingle("<span>Hello " + name + "!</span>");
            return Renderer.create("*", element);
        }

        public Renderer setProfile() {
            Renderer render = new GoThroughRenderer();
            render.add("p#name span", "asta4d");
            render.add("p#age span", "20");
            return render;
        }
    }
    ```
-   Testable Rendering logic

    Since all the rendering logics are being held by a Renderer instance which can be simply retrieved by invoking the target method of snippet class, unit test can be simply performed.

    ```java
        // prepare test target
        Renderer render = new GoThroughRenderer();
        render.add("#someIdForInt", 12345);

        // perform test
        RendererTester tester = RendererTester.forRenderer(render);
        Assert.assertEquals(tester.get("#someIdForInt"), 12345);

    ```

    Rendering for list data can be performed as well

    ```java
        // prepare test target
        Renderer render = new GoThroughRenderer();
        render.add("#someIdForInt", Arrays.asList(123, 456, 789));

        // perform test
        RendererTester tester = RendererTester.forRenderer(render);
        Assert.assertEquals(tester.getAsList("#someIdForInt"), Arrays.asList(123, 456, 789));

    ```

    [Further samples for test](https://github.com/astamuse/asta4d/blob/develop/asta4d-core/src/test/java/com/astamuse/asta4d/test/unit/RenderTesterTest.java)

-   High security of being immune from cross-site(XSS/CSRF)
    
    Asta4D is, by nature, immune from cross-site problems. You do not need to take care of cross-site any more. All the rendered value would be escaped by default and your clients have no chance to put malicious contents to your server.
    
-   View first and URL matching

    There is no a controller which dispatches requests. All the requests will be dispatched by a sort of predefined 
    URL matching rules and all the data query logic should be implemented at the snippet class which we mentioned above.

    ```java
    rules.add("/app/", "/templates/index.html");

    rules.add("/app/handler")
         .handler(LoginHandler.class)
         .handler(EchoHandler.class)
         .forward(LoginFailure.class, "/templates/error.html")
         .forward("/templates/success.html");
    ```

-   Request handlers for ajax and Restful request
    
    json request:

    ```java
    rules.add("/app/ajax/getUserList").handler(GetUserListHandler.class).json();
    ```

    ```java
    public class GetUserListHandler {

        @RequestHandler
        public List<String> queryUserList() {
            return Arrays.asList("otani", "ryu", "mizuhara");
        }
    }
    ```

    Restful request:

    ```java
    rules.add(PUT, "/app/ajax/addUser").handler(AddUserHandler.class).rest();
    ```

    ```java
    public class AddUserHandler {

        @RequestHandler
        public HeaderInfo doAdd(String newUserName) {
            // some logic that should add a new user by the given name
            // ...
            return new HeaderInfo(200);
        }
    ```

-   Multi-Thread rendering

    parallel snippet rendering: All the snippet marked as afd:parallel” or “parallel” will be executed parallel.
    
    ```html
    <div afd:render="ParallelTest$TestRender:snippetInDiv" afd:parallel>
        <div id="test">xx</div>
    </div>

    <afd:snippet render="ParallelTest$TestRender:snippetReplaceDiv" parallel>
        <div id="test">xx</div>
    </afd:snippet>
    ```    

    parallel list rendering: A parallel data convertor can be used for parallel rendering a list.
    
    ```java
    Renderer renderer = Renderer.create("div#test", list, 
        new ParallelDataConvertor<String, String>() {
                @Override
                public String convert(String obj) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return obj + "-sleep";
                }
            });
    ```

## Quick start
We are working for creating a maven archetype, before it finished, the quickest way to start with Asta4D is 
[download our sample project](http://astamuse.github.io/asta4d/download/asta4d-sample_0.8.6.zip)
as a prototype. Asta4D projects is structured by Maven 3, so you have to install Maven 3 at first. After install Maven 3, extract your
downloaded file and go to the root foler(the folder with pom.xml file), start the sample project by the following command:

    ```
    maven jetty:run
    ```

Then you can access the sample project by http://localhost:8080, there are source samples shown, it is a good start from reading the samples.
After you confirm the sample project is OK, you can add your own url mapping rules to /src/main/java/com/astamuse/asta4d/sample/UrlRules.java,
and also you can add your own html template files to /src/main/webapp.

One last thing, do not forget modify the groupId and artifactId in pom file, as well as the version. 

Additionally, there is an [English document](http://astamuse.github.com/asta4d/userguide/index.html) which describes how 
Asta4D works in more details, and there is also a [Japanese document](http://astamuse.github.com/asta4d/userguide/index_jp.html)
which includes more detailed user guide which is for our employees.

## Todo list

Immediate tasks: 

-   cachable snippet

    (a rendered snippet result should can be cached)

-   Rendering helper for validation
    
    (not implementing validaiton which should use third-party implementations such as [JSR 303](http://beanvalidation.org/), just help rendering validation result easier)


## Questions and answers

-   **Q**: Who developed Asta4D and what its current status is?
    
    **A**: Asta4D is powered by [astamuse company Ltd.](http://www.astamuse.co.jp/) locating at Tokyo Japan. We are concentrating on global innovation support 
    and develop Asta4D for our own services. Currently, Asta4D is used by our new service development and is still in alpha release status.

-   **Q**: Why are there Spring dependencies in Asta4D?
    
    **A**: Our initial purpose is to drive up our new service development, so we have to consider a balance of progression and schedule 
    between Asta4D’s development and our service’s development. So we decided to start Asta4D’s work basing on Spring MVC 
    therefore Spring MVC can do the things that we have no time to do. Currently, we have removed dependencies from Spring, but since we
    started our work from Spring MVC and therefore Asta4D can work perfectly with Spring, so our sample project is still using Spring as a sample of
    integration of Spring bean management. If you don't need Spring, simply change the dependency of "asta4d-spring" to "asta4d-web".

    
## Release Notes
-   0.8.6
    
    ADD
    - Some debug friendly message
    - Redirect now can specify code 301 or 302
    - Allow initialize asta4d Configuration from external properties file
    
    FIX
    - Refactor request chain process, now we can perfectly handle request chain
    - A bug that post process of request interceptor will be executed multiple times
    - BinaryDataUtil does not handle file path correctly
    - Context does not be initialized before dispatch

-   0.7.24
    
    ADD
    - ico MIME type
    - A empty content provider which can be used to stop the request handler chain
    
    FIX
    - GenericPathHandler should get access url from Context
    - A bug when selector not found on rendering

-   0.7.22
    
    ADD
    - A request handler can be set as generic as request interceptor.
    - Access URL can be rewritten.
    - URL Rule can be rewritten.
    - A @ContextDataSet can be used for collecting context variables in a single class, eg. form parameters.(This is a base for form validation mechanism in furture)
    
    Remove
    - Depencies of Spring MVC is no longer necessary. Asta4dServlet can be used for handling http requests and StaticResourceHandler can be used for static resource files.



## LICENSE

Apache License, Version 2.0

    Copyright 2012 astamuse company,Ltd.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.