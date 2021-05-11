<template>
    <article class="setting-basic-home">
        <section class="basic-main">
            <h5 class="main-title">Config listening events</h5>
            <section class="main-checkbox">
                <bk-checkbox :value="'value1'" class="checkbox">Build pushed branches</bk-checkbox>
                <bk-checkbox :value="'value2'" class="checkbox">Build pushed merge request</bk-checkbox>
            </section>

            <h5 class="main-title">Config merge request</h5>
            <section class="main-checkbox">
                <bk-checkbox :value="'value1'" class="checkbox">Lock MR merge</bk-checkbox>
            </section>
        </section>

        <bk-button theme="primary" class="basic-btn">保存</bk-button>
    </article>
</template>

<script>
    export default {
        
    }
</script>

<style lang="postcss" scoped>
    .setting-basic-home {
        padding: 20px 24px;
    }
    .basic-main {
        background: #fff;
        overflow: hidden;
        padding: 0 24px;
        .main-title {
            margin: 25px 0 18px;
            font-size: 16px;
            color: #313328;
        }
        .main-checkbox {
            padding-bottom: 25px;
            &:nth-child(2) {
                border-bottom: 1px solid #f0f1f5;
            }
        }
        .checkbox:not(:last-child) {
            display: block;
            margin-bottom: 17px;
        }
    }
    .basic-btn {
        margin-top: 20px;
        width: 88px;
    }
</style>
