const state = { posts: [], status: "ALL" };
const list = document.querySelector("#post-list");
const empty = document.querySelector("#empty-state");
const composer = document.querySelector("#composer");
const form = document.querySelector("#composer-form");
const toast = document.querySelector("#toast");

const api = async (path, options = {}) => {
  const response = await fetch(path, { headers: { "Content-Type": "application/json" }, ...options });
  if (!response.ok) {
    const problem = await response.json().catch(() => ({ message: "Something went wrong" }));
    throw new Error(problem.message || "Something went wrong");
  }
  return response.status === 204 ? null : response.json();
};

const notify = (message) => {
  toast.textContent = message;
  toast.classList.add("show");
  window.setTimeout(() => toast.classList.remove("show"), 2800);
};

const dateParts = (value) => {
  if (!value) return { day: "—", month: "OPEN" };
  const date = new Date(value);
  return { day: String(date.getDate()).padStart(2, "0"), month: date.toLocaleString("en", { month: "short" }).toUpperCase() };
};

const escapeHtml = (value) => value.replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" }[char]));

const render = () => {
  const posts = state.posts.filter((post) => state.status === "ALL" || post.status === state.status);
  document.querySelector("#scheduled-stat").textContent = state.posts.filter((post) => post.status === "SCHEDULED").length;
  document.querySelector("#draft-stat").textContent = state.posts.filter((post) => post.status === "DRAFT").length;
  document.querySelector("#draft-count").textContent = state.posts.filter((post) => post.status === "DRAFT").length;
  list.innerHTML = posts.map((post) => {
    const date = dateParts(post.scheduledFor);
    const channelClass = post.platform.toLowerCase();
    return `<article class="post-card"><div class="post-date"><strong>${date.day}</strong>${date.month}</div><div class="post-copy"><h3>${escapeHtml(post.title)}</h3><p>${escapeHtml(post.content)}</p><span class="post-status ${post.status.toLowerCase()}">${post.status === "DRAFT" ? "Draft" : "Scheduled"}</span></div><div class="post-channel"><span class="${channelClass}"></span>${post.platform}${post.scheduledFor ? ` · ${new Date(post.scheduledFor).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })}` : ""}</div><button class="post-menu" data-delete="${post.id}" aria-label="Delete post">···</button></article>`;
  }).join("");
  empty.hidden = posts.length > 0;
};

const loadPosts = async () => { state.posts = await api("/api/posts"); render(); };

document.querySelectorAll(".filter").forEach((button) => button.addEventListener("click", () => {
  document.querySelector(".filter.active").classList.remove("active");
  button.classList.add("active");
  state.status = button.dataset.status;
  render();
}));
document.querySelectorAll("#new-post, #empty-create").forEach((button) => button.addEventListener("click", () => composer.showModal()));
document.querySelectorAll(".idea-card").forEach((button) => button.addEventListener("click", () => {
  composer.showModal();
  form.elements.title.value = button.dataset.topic;
  form.elements.content.value = `I have been thinking about ${button.dataset.topic.toLowerCase()}. `;
}));
document.querySelector("#generate").addEventListener("click", async () => {
  const topic = document.querySelector("#ai-topic").value.trim();
  if (!topic) return notify("Add a topic first, then let AI shape it.");
  try {
    const generated = await api("/api/posts/generate", { method: "POST", body: JSON.stringify({ topic, platform: form.elements.platform.value, tone: "warm" }) });
    form.elements.title.value = generated.title;
    form.elements.content.value = generated.content;
    await loadPosts();
    notify("A starting point is ready — make it yours.");
  } catch (error) { notify(error.message); }
});
form.addEventListener("submit", async (event) => {
  event.preventDefault();
  const data = new FormData(form);
  const payload = Object.fromEntries(data.entries());
  payload.scheduledFor = payload.scheduledFor || null;
  try {
    await api("/api/posts", { method: "POST", body: JSON.stringify(payload) });
    form.reset();
    composer.close();
    await loadPosts();
    notify("Post added to your content queue.");
  } catch (error) { notify(error.message); }
});
list.addEventListener("click", async (event) => {
  const button = event.target.closest("[data-delete]");
  if (!button || !window.confirm("Delete this post?")) return;
  try { await api(`/api/posts/${button.dataset.delete}`, { method: "DELETE" }); await loadPosts(); notify("Post removed."); } catch (error) { notify(error.message); }
});
document.querySelector(".mobile-menu").addEventListener("click", () => document.querySelector(".sidebar").classList.toggle("open"));
loadPosts().catch((error) => notify(error.message));
