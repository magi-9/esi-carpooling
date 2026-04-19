<template>
  <div>
    <nav style="background:#2c3e50;padding:12px 24px;display:flex;gap:20px;align-items:center">
      <span style="color:white;font-weight:bold;font-size:18px">Carpooling</span>
      <RouterLink to="/search" style="color:#ecf0f1;text-decoration:none">Search Rides</RouterLink>
    </nav>
    <RouterView />
  </div>
</template>
