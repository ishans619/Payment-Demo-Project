import streamlit as st
import requests

BASE_URL = "http://localhost:8080/api"

st.set_page_config(
    page_title="Payment Demo UI",
    page_icon="💳",
    layout="wide",
    initial_sidebar_state="expanded"
)

if "theme_mode" not in st.session_state:
    st.session_state.theme_mode = "dark"
if "last_order_id" not in st.session_state:
    st.session_state.last_order_id = None
if "last_payment_reference" not in st.session_state:
    st.session_state.last_payment_reference = None
if "last_payment_response" not in st.session_state:
    st.session_state.last_payment_response = None


def apply_theme():
    dark = st.session_state.theme_mode == "dark"
    bg = "#0E1117" if dark else "#F7F9FC"
    card = "#161B22" if dark else "#FFFFFF"
    text = "#E6EDF3" if dark else "#111827"
    muted = "#9CA3AF" if dark else "#4B5563"
    border = "#30363D" if dark else "#D1D5DB"
    accent = "#22C55E" if dark else "#2563EB"
    danger = "#F87171" if dark else "#DC2626"

    st.markdown(f"""
    <style>
    .stApp {{
        background: {bg};
        color: {text};
    }}
    .block-container {{
        padding-top: 1.6rem;
        padding-bottom: 2rem;
        max-width: 1200px;
    }}
    h1, h2, h3, h4, h5, h6, p, label, div {{
        color: {text};
    }}
    [data-testid="stSidebar"] {{
        background: {card};
        border-right: 1px solid {border};
    }}
    .app-card {{
        background: {card};
        border: 1px solid {border};
        border-radius: 18px;
        padding: 1.1rem 1rem;
        margin-bottom: 1rem;
        box-shadow: 0 8px 30px rgba(0,0,0,0.08);
    }}
    .hero {{
        background: linear-gradient(135deg, {card}, {bg});
        border: 1px solid {border};
        border-radius: 22px;
        padding: 1.4rem 1.2rem;
        margin-bottom: 1rem;
    }}
    .muted {{ color: {muted}; }}
    .pill {{
        display: inline-block;
        padding: 0.35rem 0.7rem;
        border-radius: 999px;
        background: {accent}20;
        color: {accent};
        border: 1px solid {accent}55;
        font-size: 0.85rem;
        margin-right: 0.5rem;
        margin-bottom: 0.4rem;
    }}
    .stButton > button {{
        border-radius: 12px;
        height: 2.9em;
        font-weight: 700;
        border: 1px solid {border};
    }}
    </style>
    """, unsafe_allow_html=True)


apply_theme()

with st.sidebar:
    st.title("💳 Payment Demo")
    st.caption("Spring Boot + Streamlit")
    if st.toggle("Dark mode", value=st.session_state.theme_mode == "dark"):
        st.session_state.theme_mode = "dark"
    else:
        st.session_state.theme_mode = "light"

    st.markdown("---")
    st.write("**Quick state**")
    st.write(f"Last Order ID: {st.session_state.last_order_id}")
    st.write(f"Last Payment Ref: {st.session_state.last_payment_reference}")

st.markdown("""
<div class='hero'>
  <h1>Payment Integration Demo Dashboard</h1>
  <span class='pill'>Orders</span>
  <span class='pill'>Payments</span>
  <span class='pill'>Webhooks</span>
  <span class='pill'>State tracking</span>
</div>
""", unsafe_allow_html=True)

col1, col2 = st.columns(2)

with col1:
    st.markdown("<div class='app-card'>", unsafe_allow_html=True)
    st.subheader("Create Order")
    with st.form("create_order_form"):
        product_name = st.text_input("Product Name", placeholder="Phone")
        amount = st.number_input("Amount", min_value=1, value=20000, step=1)
        order_submit = st.form_submit_button("Create Order")

        if order_submit:
            try:
                response = requests.post(
                    f"{BASE_URL}/orders",
                    params={"productName": product_name, "amount": amount},
                    timeout=10
                )
                if response.ok:
                    data = response.json()
                    st.session_state.last_order_id = data.get("id")
                    st.success("Order created successfully")
                    st.json(data)
                else:
                    st.error(f"Failed to create order: {response.status_code}")
                    st.json(response.json())
            except Exception as e:
                st.error(f"Error: {e}")
    st.markdown("</div>", unsafe_allow_html=True)

with col2:
    st.markdown("<div class='app-card'>", unsafe_allow_html=True)
    st.subheader("Create Payment")
    default_order_id = int(st.session_state.last_order_id) if st.session_state.last_order_id else 1
    with st.form("create_payment_form"):
        order_id = st.number_input("Order ID", min_value=1, value=default_order_id, step=1)
        idempotency_key = st.text_input("Idempotency Key", placeholder="abc123")
        payment_submit = st.form_submit_button("Create Payment")

        if payment_submit:
            try:
                response = requests.post(
                    f"{BASE_URL}/payments",
                    params={"orderId": order_id, "idempotencyKey": idempotency_key},
                    timeout=10
                )
                if response.ok:
                    data = response.json()
                    st.session_state.last_payment_reference = data.get("paymentReference")
                    st.session_state.last_payment_response = data
                    st.success("Payment created successfully")
                    st.json(data)
                else:
                    st.error(f"Failed to create payment: {response.status_code}")
                    st.json(response.json())
            except Exception as e:
                st.error(f"Error: {e}")
    st.markdown("</div>", unsafe_allow_html=True)

col3, col4 = st.columns(2)

with col3:
    st.markdown("<div class='app-card'>", unsafe_allow_html=True)
    st.subheader("Get Order By ID")
    lookup_order_id = st.number_input(
        "Enter Order ID",
        min_value=1,
        value=int(st.session_state.last_order_id) if st.session_state.last_order_id else 1,
        step=1,
        key="lookup_order_id"
    )
    if st.button("Fetch Order", use_container_width=True):
        try:
            response = requests.get(f"{BASE_URL}/orders/{lookup_order_id}", timeout=10)
            if response.ok:
                st.success("Order fetched successfully")
                st.json(response.json())
            else:
                st.error(f"Failed to fetch order: {response.status_code}")
                st.json(response.json())
        except Exception as e:
            st.error(f"Error: {e}")
    st.markdown("</div>", unsafe_allow_html=True)

with col4:
    st.markdown("<div class='app-card'>", unsafe_allow_html=True)
    st.subheader("Get Payment By Reference")
    payment_reference = st.text_input(
        "Enter Payment Reference",
        value=st.session_state.last_payment_reference if st.session_state.last_payment_reference else "",
        placeholder="PAY_xxx"
    )
    if st.button("Fetch Payment", use_container_width=True):
        try:
            response = requests.get(f"{BASE_URL}/payments/{payment_reference}", timeout=10)
            if response.ok:
                st.success("Payment fetched successfully")
                st.json(response.json())
            else:
                st.error(f"Failed to fetch payment: {response.status_code}")
                st.json(response.json())
        except Exception as e:
            st.error(f"Error: {e}")
    st.markdown("</div>", unsafe_allow_html=True)

col5, col6 = st.columns(2)

with col5:
    st.markdown("<div class='app-card'>", unsafe_allow_html=True)
    st.subheader("Order Details")
    details_order_id = st.number_input(
        "Order ID for Details",
        min_value=1,
        value=int(st.session_state.last_order_id) if st.session_state.last_order_id else 1,
        step=1,
        key="details_order_id"
    )
    if st.button("Fetch Order Details", use_container_width=True):
        try:
            response = requests.get(f"{BASE_URL}/orders/{details_order_id}/details", timeout=10)
            if response.ok:
                st.success("Order details fetched successfully")
                st.json(response.json())
            else:
                st.error(f"Failed to fetch order details: {response.status_code}")
                st.json(response.json())
        except Exception as e:
            st.error(f"Error: {e}")
    st.markdown("</div>", unsafe_allow_html=True)

with col6:
    st.markdown("<div class='app-card'>", unsafe_allow_html=True)
    st.subheader("Payment History By Order")
    history_order_id = st.number_input(
        "Order ID for Payment History",
        min_value=1,
        value=int(st.session_state.last_order_id) if st.session_state.last_order_id else 1,
        step=1,
        key="history_order_id"
    )
    if st.button("Fetch Payment History", use_container_width=True):
        try:
            response = requests.get(f"{BASE_URL}/orders/{history_order_id}/payments", timeout=10)
            if response.ok:
                st.success("Payment history fetched successfully")
                st.json(response.json())
            else:
                st.error(f"Failed to fetch payment history: {response.status_code}")
                st.json(response.json())
        except Exception as e:
            st.error(f"Error: {e}")
    st.markdown("</div>", unsafe_allow_html=True)

st.markdown("<div class='app-card'>", unsafe_allow_html=True)
st.subheader("Webhook Simulation")

webhook_payment_reference = st.text_input(
    "Webhook Payment Reference",
    value=st.session_state.last_payment_reference if st.session_state.last_payment_reference else "",
    key="webhook_payment_reference"
)

c1, c2, c3 = st.columns(3)

with c1:
    if st.button("Simulate Success", type="primary", use_container_width=True):
        try:
            response = requests.post(
                f"{BASE_URL}/webhook",
                json={"paymentReference": webhook_payment_reference, "eventType": "payment_intent.succeeded"},
                timeout=10
            )
            if response.ok:
                data = response.json()
                st.session_state.last_payment_response = data
                st.success("Success webhook processed")
                st.json(data)
            else:
                st.error(f"Failed: {response.status_code}")
                st.json(response.json())
        except Exception as e:
            st.error(f"Error: {e}")

with c2:
    if st.button("Simulate Failed", use_container_width=True):
        try:
            response = requests.post(
                f"{BASE_URL}/webhook",
                json={"paymentReference": webhook_payment_reference, "eventType": "payment_intent.payment_failed"},
                timeout=10
            )
            if response.ok:
                data = response.json()
                st.session_state.last_payment_response = data
                st.success("Failure webhook processed")
                st.json(data)
            else:
                st.error(f"Failed: {response.status_code}")
                st.json(response.json())
        except Exception as e:
            st.error(f"Error: {e}")

with c3:
    retry_order_id = st.session_state.last_order_id if st.session_state.last_order_id else 1
    if st.button("Retry Payment", use_container_width=True):
        try:
            response = requests.post(f"{BASE_URL}/orders/{retry_order_id}/payments/retry", timeout=10)
            if response.ok:
                data = response.json()
                st.session_state.last_payment_reference = data.get("paymentReference")
                st.session_state.last_payment_response = data
                st.success("Retry payment created")
                st.json(data)
            else:
                st.error(f"Retry failed: {response.status_code}")
                st.json(response.json())
        except Exception as e:
            st.error(f"Error: {e}")

st.markdown("</div>", unsafe_allow_html=True)

st.markdown("<div class='app-card'>", unsafe_allow_html=True)
st.subheader("Current Session State")

left, right = st.columns(2)

with left:
    st.write("**Last Order ID**")
    st.write(st.session_state.last_order_id)

    st.write("**Last Payment Reference**")
    st.write(st.session_state.last_payment_reference)

with right:
    st.write("**Last Payment Response**")
    if st.session_state.last_payment_response:
        st.json(st.session_state.last_payment_response)
    else:
        st.info("No payment response stored yet")

st.markdown("</div>", unsafe_allow_html=True)