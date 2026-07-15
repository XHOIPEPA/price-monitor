import random
from datetime import datetime
from http.server import BaseHTTPRequestHandler, HTTPServer

# Produktet fillestare: id -> (titull, cmim bazë)
PRODUCTS = {
    1: ("Laptop Pro 15", 1200.00),
    2: ("Kufje Wireless", 89.50),
    3: ("Tastiere Mekanike", 145.00),
}

# Gjendja aktuale (ndryshon me kohen)
state = {pid: {"price": base, "in_stock": True} for pid, (_, base) in PRODUCTS.items()}


def maybe_change(pid):
    """Me nje probabilitet te caktuar, ndryshon cmimin ose stock-un."""
    s = state[pid]
    r = random.random()

    if r < 0.35:  # 35% shanse per ndryshim cmimi
        delta = random.uniform(-0.15, 0.15)          # +/- 15%
        s["price"] = round(s["price"] * (1 + delta), 2)
    elif r < 0.45:  # 10% shanse per ndryshim stock-u
        s["in_stock"] = not s["in_stock"]


HTML = """<!DOCTYPE html>
<html>
<head><title>{title}</title></head>
<body>
  <div class="product_main">
    <h1>{title}</h1>
    <p class="price_color">£{price:.2f}</p>
    <p class="availability">{availability}</p>
  </div>
  <p><small>Gjeneruar: {ts}</small></p>
</body>
</html>"""


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        # Pret rruge te formes /product/1
        parts = self.path.strip("/").split("/")
        if len(parts) != 2 or parts[0] != "product" or not parts[1].isdigit():
            self.send_error(404, "Not found")
            return

        pid = int(parts[1])
        if pid not in PRODUCTS:
            self.send_error(404, "Product not found")
            return

        maybe_change(pid)
        title = PRODUCTS[pid][0]
        s = state[pid]
        availability = "In stock (12 available)" if s["in_stock"] else "Out of stock"

        body = HTML.format(
            title=title,
            price=s["price"],
            availability=availability,
            ts=datetime.now().isoformat(timespec="seconds"),
        ).encode("utf-8")

        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        print(f"[mock-shop] {self.address_string()} {fmt % args}")


if __name__ == "__main__":
    print("Mock shop po degjon ne portin 8000...")
    HTTPServer(("0.0.0.0", 8000), Handler).serve_forever()