(() => {
  const panel = document.getElementById("trip-location");
  if (!panel) return;

  const interval = Number(panel.dataset.pollMs) || 5000;
  const latitude = document.getElementById("location-latitude");
  const longitude = document.getElementById("location-longitude");
  const updated = document.getElementById("location-updated");
  const sync = document.getElementById("location-sync");
  let lastTripStatus = "IN_PROGRESS";

  async function refreshTrip() {
    try {
      const response = await fetch("/ui/trip/status", {
        headers: { Accept: "application/json" },
        credentials: "same-origin",
      });
      if (!response.ok) throw new Error("status");
      const state = await response.json();
      const tripStatus = state.trip?.status;
      if (tripStatus && tripStatus !== lastTripStatus) {
        window.location.reload();
        return;
      }
      const location = state.location;
      if (!location) return;
      latitude.textContent = Number(location.latitude).toFixed(6);
      longitude.textContent = Number(location.longitude).toFixed(6);
      updated.textContent = new Date(location.recordedAt).toLocaleString("es-AR");
      sync.textContent = location.m4Synced
        ? "✓ Ubicación sincronizada con M4"
        : "No fue posible sincronizar la ubicación con M4.";
      sync.className = location.m4Synced ? "notice success" : "notice error";
    } catch (_error) {
      sync.textContent = "No fue posible consultar la ubicación actual.";
      sync.className = "notice error";
    }
  }

  refreshTrip();
  window.setInterval(refreshTrip, interval);
})();
