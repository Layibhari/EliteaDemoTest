<script lang="ts">
  import type { Component } from "svelte";

  let { href, icon, label, active = false }: {
    href: string;
    icon: Component;
    label: string;
    active?: boolean;
  } = $props();

  let linkClass = $derived(
    "nav-link flex items-center gap-2 uppercase no-underline" + (active ? " active" : "")
  );
</script>

<li>
  <a {href} class={linkClass} title={label}>
    <!-- svelte-ignore svelte_component_deprecated -->
    <svelte:component this={icon} size={16} />
    <span>{label}</span>
  </a>
</li>

<style>
  .nav-link {
    font-family: "Montserrat", sans-serif;
    font-size: 14px;
    text-shadow: none;
    padding: 28px 20px;
    transition: all 0.15s;
    color: #f1f1f1;
    text-decoration: none;
  }
  .nav-link:hover {
    color: #eeeeee;
    background-color: #6db33f;
  }
  .nav-link.active {
    background-color: #6db33f;
  }
</style>
