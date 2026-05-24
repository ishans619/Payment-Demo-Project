import streamlit as st

if "theme_mode" not in st.session_state:
    st.session_state.theme_mode = "dark"


def apply_theme():
    dark = st.session_state.theme_mode == "dark"
    bg = "#0E1117" if dark else "#F7F9FC"
    card = "#161B22" if dark else "#FFFFFF"
    text = "#E6EDF3" if dark else "#111827"
    muted = "#9CA3AF" if dark else "#4B5563"
    border = "#30363D" if dark else "#D1D5DB"
    accent = "#22C55E" if dark else "#2563EB"

    st.markdown(f"""
    <style>
    .stApp {{ background: {bg}; color: {text}; }}
    .block-container {{ padding-top: 1.6rem; max-width: 1100px; }}
    h1,h2,h3,h4,h5,h6,p,li,div {{ color: {text}; }}
    [data-testid="stSidebar"] {{ background: {card}; border-right: 1px solid {border}; }}
    .hero {{
        background: linear-gradient(135deg, {card}, {bg});
        border: 1px solid {border};
        border-radius: 22px;
        padding: 1.4rem 1.2rem;
        margin-bottom: 1rem;
    }}
    .endpoint {{
        background: {card};
        border: 1px solid {border};
        border-radius: 18px;
        padding: 1rem;
        margin-bottom: 0.9rem;
    }}
    .method {{
        display:inline-block;
        padding:0.25rem 0.6rem;
        border-radius:999px;
        background:{accent}20;
        color:{accent};
        border:1px solid {accent}55;
        font-size:0.82rem;
        font-weight:700;
        margin-right:0.5rem;
    }}
    .muted {{ color: {muted}; }}
    </style>
    """, unsafe_allow_html=True)


apply_theme()

with st.sidebar:
    st.title("📘 Project Info")
    if st.toggle("Dark mode", value=st.session_state.theme_mode == "dark"):
        st.session_state.theme_mode = "dark"
    else:
        st.session_state.theme_mode = "light"

st.markdown("""
<div class='hero'>
  <h1>Why this project exists</h1>
  <p class='muted'>This project was built to move beyond basic CRUD and understand how a real backend handles payment lifecycle, idempotency, retries, asynchronous webhook-style updates, error handling, and state-driven APIs. It is a hands-on learning project inspired by practical backend patterns used in payment systems.</p>
</div>
""", unsafe_allow_html=True)

st.subheader("Purpose")
st.write(
    "This demo helps you understand how an order and its payment evolve over time instead of behaving like isolated database rows. "
    "It is meant to teach backend design ideas such as idempotency, transactional updates, retry rules, DTO-based responses, combined read APIs, and webhook-driven state changes."
)

st.subheader("Endpoints")

endpoints = [
    ("POST", "/api/orders", "Creates a new order with product and amount details. This is the starting point of the payment flow and gives you an order ID that later APIs use."),
    ("POST", "/api/payments", "Creates a payment attempt for a given order using an idempotency key. It simulates safe payment creation where duplicate requests should not create duplicate payment records."),
    ("POST", "/api/webhook", "Simulates asynchronous payment provider callbacks such as success or failure events. This endpoint updates payment and order state the way a real webhook-driven integration would."),
    ("GET", "/api/orders/{id}", "Fetches a single order by its ID. It helps inspect the current order state after payment creation, failure, success, or retry events."),
    ("GET", "/api/payments/{paymentReference}", "Fetches a payment using its payment reference. It is useful for tracking the latest payment state after webhook updates."),
    ("GET", "/api/orders/{id}/details", "Returns combined order and payment details in one response. This is useful when a client wants the full picture of an order and its linked payment together."),
    ("GET", "/api/orders/{id}/payments", "Returns payment history for an order. It helps inspect all payment attempts made for the same order, including retries."),
    ("POST", "/api/orders/{id}/payments/retry", "Creates a new payment attempt only when retry rules allow it. This prevents unsafe retries and models controlled recovery after failed payments.")
]

for method, path, desc in endpoints:
    st.markdown(
        f"""
        <div class='endpoint'>
          <span class='method'>{method}</span><strong>{path}</strong>
          <p class='muted' style='margin-top:0.7rem;'>{desc}</p>
        </div>
        """,
        unsafe_allow_html=True
    )

st.subheader("What this project teaches")
st.markdown("""
- How to move from CRUD APIs to lifecycle-driven backend design.
- How to use idempotency, retry guards, exception handling, DTOs, and transactions.
- How to expose cleaner read APIs that show current state and payment history.
""")