<template>
    <section>
        <div id="modal-root"></div>
        <section class="hero">
            <div class="hero--content">
                <div class="container">
                    <div class="hero--text-block">
                        <h1 class="hero--title">一个更容易<strong> 治理 </strong>的 CI 服务</h1>
                        <div>Stream 服务可以在主流的操作系统上持续快速地编译、测试、部署你的服务。<br>
                            让你聚焦于编码本身，而非权限申请、构建机准备、发布沟通等琐事。
                        </div>
                        <div class="hero-cta">
                            <a href="" class="button big black" data-proofer-ignore="">
                                在 Stream 上运行一个 "Hello World"
                            </a>
                            <a href="https://iwiki.woa.com/x/pZMdK" target="_blank" class="button big " data-proofer-ignore="">
                                了解更多特性
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <section id="your-site-your-workflow" class="section section-roots">
            <div class="container">
                <div class="feature-block ltr" style="display: flex;justify-content: space-between;">
                    <div class="feature-block--copy">
                        <h2 class="section--title" style="">
                            语法对齐GitHub Actions<br>
                            将语法优雅进行到底！
                        </h2>
                        <div class="section--sub">
                            <p>
                                在蓝盾 Stream 服务中，最直接与开发者打交道的就是 YAML 语法，我们在调研了 Azure Pipelines / Gitlab CI / Travis CI / Circle CI 等行业主流 CI YAML语法后最终决定：蓝盾 Stream 语法将100%兼容 GitHub Actions ！
                            </p>
                            <p>
                                如果你已经对 GitHub Actions 有过一定了解，那么不要犹豫了，来运行你的第一个Hello World吧！
                            </p>
                            <div class="hero-cta" style="margin-top: 20px;">
                                <a href="https://iwiki.woa.com/x/4FPpK" target="_blank" class="button big black" data-proofer-ignore="">
                                    点击查阅完整 YAML 语法
                                </a>
                            </div>
                        </div>
                    </div>
                    <div class="feature-block--illu illu-shadow--right">
                        <img loading="lazy" src="./../images/home/yaml.png" style="width:410px;height:554px;">
                    </div>
                </div>
            </div>
        </section>

        <section id="trusted-by" class="section section-logos">
            <div class="container">
                <span class="logos--title">联合出品方</span>
                <div class="logos--container">
                    <img src="./../images/home/bk-ci.png" class="logos--logo" width="auto" height="60" style="margin-top:0px;" alt="蓝盾">
                    <img src="./../images/home/tgit-with.png" class="logos--logo" width="auto" height="60" style="margin-top:0px;" alt="工蜂">
                    <img src="./../images/home/tencent_ci.png" class="logos--logo" width="auto" height="50" style="margin-top:0px;" alt="腾讯CI">
                    <img src="./../images/home/EP.png" class="logos--logo" width="auto" height="60" style="margin-top:-1px;" alt="EP度量">
                </div>
            </div>
        </section>

        <section id="empower-your-editors" class="section section-sync">
            <div class="container">
                <div class="feature-block rtl">
                    <div class="feature-block--illu illu-shadow--left">
                        <img loading="lazy" src="./../images/home/home.png" data-sizes="(max-width: 76px) 100%, 800px" class=" lazyloaded" sizes="(max-width: 76px) 100%, 800px">
                    </div>
                    <div class="feature-block--copy">
                        <h2 class="section--title" style="">代码与UI，二者皆可得</h2>
                        <div class="section--sub">
                            <p>
                                在 Stream 里，你可以用 YAML 代码的方式描述出你的 CI 流水线，这样可以将流水线的配置变更也纳入日常的Git工作流中。
                            </p>
                            <p>
                                同时，因为与蓝盾复用了相同的 CI 引擎，所以依然可以保持与蓝盾图形化 UI 相同的体验，在出问题时可快速定位。
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <section id="own-your-content" class="section section-automated">
            <div class="container">
                <div class="feature-block ltr">
                    <div class="feature-block--copy">
                        <h2 class="section--title" style="">丰富的腾讯 CI 插件生态</h2>
                        <div class="section--sub">
                            <p>蓝盾研发商店自发布以来，共上架<strong>1250</strong>个流水线插件</p>
                            <p>在 Stream 中，你可以通过<em>uses</em>关键字方便引入这些插件，让插件生态得以最大化利用</p>
                        </div>
                    </div>
                    <div class="feature-block--illu illu-shadow--right">
                        <img loading="lazy" src="./../images/home/store.jpg" data-sizes="(max-width: 76px) 100%, 800px" class=" lazyloaded" sizes="(max-width: 76px) 100%, 800px">
                    </div>
                </div>
            </div>
        </section>

        <section class="section section-cta center">
            <div class="container">
                <h2 class="section--title">快速高质量地交付代码</h2>
                <p class="section--sub is-center">用最好的 CI 工具，交付更好的代码</p>
                <br><br>
                <a href="" class="button black">为你的工程开启 CI</a>
            </div>
        </section>

        <footer class="footer center">
            <div class="lower-footer">
                <div class="container">
                    <div>Copyright © 2012-2021 Tencent BlueKing. All Rights Reserved</div>
                </div>
            </div>
        </footer>
    </section>
</template>

<style lang="postcss">
    @import '@/css/home';
</style>
