<script lang="ts">
  import { House, Search, Stethoscope, AlertTriangle } from "lucide-svelte";
  import NavLink from "./NavLink.svelte";

  let { children } = $props();
</script>

<nav class="navbar">
  <div class="navbar-inner">
    <a class="navbar-brand" href="/" aria-label="PetClinic home">
      <span></span>
    </a>
    <ul class="navbar-nav">
      <NavLink href="/#/welcome" icon={House} label="Home" active={true} />
      <NavLink href="/owners/find" icon={Search} label="Find Owners" />
      <NavLink href="/vets.html" icon={Stethoscope} label="Veterinarians" />
      <NavLink href="/oups" icon={AlertTriangle} label="Error" />
    </ul>
  </div>
</nav>

<div class="content-wrapper">
  <div class="xd-container">
    <main>
      {#if children}
        {@render children()}
      {/if}
    </main>

    <br />
    <br />
    <div class="footer">
      <img src="/resources/images/spring-logo.svg" alt="VMware Tanzu Logo" class="logo" />
    </div>
  </div>
</div>

<style>
  .navbar {
    border-top: 4px solid #6db33f;
    background-color: #34302d;
    padding: 0 1rem;
  }
  .navbar-inner {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    justify-content: space-between;
  }
  .navbar-brand {
    background: url("/resources/images/spring-logo-dataflow.png") -1px -1px no-repeat;
    margin: 12px 0 6px;
    width: 229px;
    height: 46px;
    display: inline-block;
    text-decoration: none;
    padding: 0;
    position: relative;
    flex-shrink: 0;
  }
  .navbar-brand span {
    display: block;
    width: 229px;
    height: 46px;
    background: url("/resources/images/spring-logo-dataflow.png") -1px -48px no-repeat;
    opacity: 0;
    transition: opacity 0.12s ease-in-out;
  }
  .navbar a:hover.navbar-brand span {
    opacity: 1;
  }
  .navbar-nav {
    display: flex;
    flex-direction: row;
    padding-left: 0;
    margin: 0;
    list-style: none;
    flex-wrap: wrap;
  }
  .content-wrapper {
    padding: 0 15px;
  }
  .xd-container {
    margin-top: 40px;
    margin-bottom: 100px;
    padding-left: 5px;
    padding-right: 5px;
  }
  .footer {
    text-align: center;
  }
  .logo {
    width: 200px;
  }
</style>
