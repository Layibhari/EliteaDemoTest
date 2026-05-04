<script lang="ts">
  import Router from "svelte-spa-router";
  import Layout from "$lib/components/Layout.svelte";
  import Welcome from "./pages/welcome/Welcome.svelte";
  import FormsDemo from "./pages/forms/FormsDemo.svelte";

  const routes = {
    "/": Welcome,
    "/welcome": Welcome,
    "/forms": FormsDemo,
  };
</script>

<Layout>
  <Router {routes} />
</Layout>
