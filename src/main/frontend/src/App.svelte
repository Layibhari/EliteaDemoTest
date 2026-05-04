<script lang="ts">
  import Router from "svelte-spa-router";
  import Layout from "$lib/components/Layout.svelte";
  import Welcome from "./pages/welcome/Welcome.svelte";

  const routes = {
    "/": Welcome,
    "/welcome": Welcome,
  };
</script>

<Layout>
  <Router {routes} />
</Layout>
