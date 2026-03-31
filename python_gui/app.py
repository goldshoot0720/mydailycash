import json
import math
import re
import sys
import tkinter as tk
from pathlib import Path
from tkinter import messagebox, ttk

PRIZES = {
    0: 0,
    1: 0,
    2: 50,
    3: 300,
    4: 20000,
    5: 8000000,
}

TICKET_PRICE = 50


def get_index_html_path():
    if getattr(sys, "frozen", False):
        bundle_dir = Path(getattr(sys, "_MEIPASS", Path(sys.executable).resolve().parent))
        bundled_index = bundle_dir / "index.html"
        if bundled_index.exists():
            return bundled_index
        return Path(sys.executable).resolve().parent / "index.html"

    repo_root = Path(__file__).resolve().parent.parent
    return repo_root / "index.html"


INDEX_HTML = get_index_html_path()


def load_draws():
    text = INDEX_HTML.read_text(encoding="utf-8")
    match = re.search(r"const embeddedDraws = (\[[\s\S]*?\n\s*\]);", text)
    if not match:
        raise RuntimeError("無法從 index.html 載入開獎資料。")
    return json.loads(match.group(1))


DRAWS = load_draws()


class LotteryCalculator:
    @staticmethod
    def pad(number):
        return str(number).zfill(2)

    @staticmethod
    def combo(n, r):
        return math.comb(n, r)

    @staticmethod
    def visible_count(mode):
        if mode == "combo6":
            return 6
        if mode == "combo7":
            return 7
        if mode == "combo8":
            return 8
        if mode == "combo9":
            return 9
        return 5

    @staticmethod
    def is_combo_mode(mode):
        return mode in {"combo6", "combo7", "combo8", "combo9"}

    @classmethod
    def validate_numbers(cls, values, expected_count):
        if len(values) != expected_count or any(value == "" for value in values):
            raise ValueError(f"請輸入 {expected_count} 個號碼。")

        try:
            numbers = [int(value) for value in values]
        except ValueError as error:
            raise ValueError("號碼必須是整數。") from error

        if any(number < 1 or number > 39 for number in numbers):
            raise ValueError("號碼必須介於 1 到 39。")
        if len(set(numbers)) != expected_count:
            raise ValueError("號碼不可重複。")

        return [cls.pad(number) for number in sorted(numbers)]

    @classmethod
    def build_selection(cls, mode, values, pack_index=None):
        visible_count = cls.visible_count(mode)
        active_values = values[:visible_count]

        if cls.is_combo_mode(mode):
            numbers = cls.validate_numbers(active_values, visible_count)
            return {
                "mode": mode,
                "numbers": numbers,
                "combo_size": visible_count,
                "label": f"{visible_count}連碰 {' '.join(numbers)}",
            }

        if pack_index is None:
            numbers = cls.validate_numbers(active_values[:5], 5)
            return {"mode": "single", "numbers": numbers, "label": " ".join(numbers)}

        fixed_values = [value for index, value in enumerate(active_values[:5]) if index != pack_index]
        numbers = cls.validate_numbers(fixed_values, 4)
        return {
            "mode": "pack",
            "numbers": numbers,
            "pack_index": pack_index,
            "label": f"{' '.join(numbers)} + 包牌",
        }

    @classmethod
    def analyze(cls, selection):
        rows = []
        selected = set(selection["numbers"])

        for draw in DRAWS:
            draw_numbers = draw["numbers"]

            if selection["mode"] == "pack":
                fixed_hits = [number for number in draw_numbers if number in selected]
                pack_hits = [number for number in draw_numbers if number not in selected]
                fixed_count = len(fixed_hits)
                upgraded_count = fixed_count + 1
                upgraded_tickets = len(pack_hits)
                base_tickets = 35 - upgraded_tickets
                prize = upgraded_tickets * PRIZES[upgraded_count] + base_tickets * PRIZES[fixed_count]
                rows.append(
                    {
                        "issue": draw["issue"],
                        "date": draw["rocDate"],
                        "numbers": " ".join(draw_numbers),
                        "match": f"固定中 {fixed_count} 個",
                        "prize": prize,
                        "detail": "包牌命中: " + (" ".join(pack_hits) if pack_hits else "-"),
                    }
                )
                continue

            if cls.is_combo_mode(selection["mode"]):
                hits = [number for number in draw_numbers if number in selected]
                hit_count = len(hits)
                combo_size = selection["combo_size"]
                prize = 0
                for match_count in range(2, min(5, hit_count) + 1):
                    ticket_count = cls.combo(hit_count, match_count) * cls.combo(combo_size - hit_count, 5 - match_count)
                    prize += ticket_count * PRIZES[match_count]
                rows.append(
                    {
                        "issue": draw["issue"],
                        "date": draw["rocDate"],
                        "numbers": " ".join(draw_numbers),
                        "match": f"選中 {hit_count} 個",
                        "prize": prize,
                        "detail": "命中號碼: " + (" ".join(hits) if hits else "-"),
                    }
                )
                continue

            hits = [number for number in draw_numbers if number in selected]
            hit_count = len(hits)
            rows.append(
                {
                    "issue": draw["issue"],
                    "date": draw["rocDate"],
                    "numbers": " ".join(draw_numbers),
                    "match": f"對中 {hit_count} 個",
                    "prize": PRIZES[hit_count],
                    "detail": "命中號碼: " + (" ".join(hits) if hits else "-"),
                }
            )

        if selection["mode"] == "pack":
            cost_per_draw = TICKET_PRICE * 35
        elif cls.is_combo_mode(selection["mode"]):
            cost_per_draw = cls.combo(selection["combo_size"], 5) * TICKET_PRICE
        else:
            cost_per_draw = TICKET_PRICE

        prize_total = sum(row["prize"] for row in rows)
        cost_total = len(rows) * cost_per_draw
        return {
            "rows": rows,
            "draw_count": len(rows),
            "wins": sum(1 for row in rows if row["prize"] > 0),
            "cost_total": cost_total,
            "prize_total": prize_total,
            "net_total": prize_total - cost_total,
        }


class App(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("今彩539 Python GUI 版")
        self.mode_var = tk.StringVar(value="single")
        self.pack_index_var = tk.IntVar(value=-1)
        self.entries = []
        self.pack_buttons = []
        self.entry_holders = []
        self.summary_cards = []
        self.entry_grid = None
        self.summary_frame = None
        self._last_entry_columns = None
        self._last_summary_columns = None
        self._last_visible_count = None

        self._configure_window()

        self._build_ui()
        self._apply_mode()
        self._clear_results()
        self.bind("<Configure>", self._on_window_resize)

    def _configure_window(self):
        screen_width = self.winfo_screenwidth()
        screen_height = self.winfo_screenheight()
        width = min(max(int(screen_width * 0.76), 1080), 1420)
        height = min(max(int(screen_height * 0.86), 760), 940)
        x = max((screen_width - width) // 2, 0)
        y = max((screen_height - height) // 2, 0)
        self.geometry(f"{width}x{height}+{x}+{y}")
        self.minsize(1024, 700)

    def _build_ui(self):
        style = ttk.Style(self)
        try:
            style.theme_use("clam")
        except tk.TclError:
            pass

        style.configure("Header.TLabel", font=("Microsoft JhengHei", 21, "bold"))
        style.configure("Subheader.TLabel", font=("Microsoft JhengHei", 10))
        style.configure("SummaryValue.TLabel", font=("Microsoft JhengHei", 16, "bold"))
        style.configure("SummaryNote.TLabel", font=("Microsoft JhengHei", 9))

        self.configure(bg="#f6ead1")
        root = ttk.Frame(self, padding=12)
        root.pack(fill="both", expand=True)

        header = ttk.Frame(root, padding=(6, 4, 6, 8))
        header.pack(fill="x")
        ttk.Label(header, text="今彩539 Python GUI 版", style="Header.TLabel").pack(anchor="w")
        ttk.Label(header, text="支援單注、包牌、6連碰、7連碰、8連碰、9連碰收益試算。", style="Subheader.TLabel").pack(anchor="w", pady=(4, 0))

        form = ttk.LabelFrame(root, text="輸入設定", padding=12)
        form.pack(fill="x", pady=(6, 10))

        mode_row = ttk.Frame(form)
        mode_row.pack(fill="x", pady=(0, 10))
        for mode, label in [("single", "單注 / 包牌"), ("combo6", "6連碰"), ("combo7", "7連碰"), ("combo8", "8連碰"), ("combo9", "9連碰")]:
            ttk.Radiobutton(mode_row, text=label, value=mode, variable=self.mode_var, command=self._apply_mode).pack(side="left", padx=(0, 8))

        self.entry_grid = ttk.Frame(form)
        self.entry_grid.pack(anchor="w")
        for index in range(9):
            holder = ttk.Frame(self.entry_grid, padding=(4, 2))
            ttk.Label(holder, text=f"號碼 {index + 1}").pack(anchor="center")
            entry = ttk.Entry(holder, width=6, justify="center", font=("Microsoft JhengHei", 12))
            entry.pack(pady=(2, 3))
            pack_button = ttk.Button(holder, text="包牌", width=8, command=lambda idx=index: self.toggle_pack(idx))
            pack_button.pack()
            self.entries.append(entry)
            self.pack_buttons.append(pack_button)
            self.entry_holders.append(holder)

        button_row = ttk.Frame(form)
        button_row.pack(fill="x", pady=(10, 0))
        ttk.Button(button_row, text="開始比對", command=self.analyze).pack(side="left")
        ttk.Button(button_row, text="帶入範例", command=self.fill_sample).pack(side="left", padx=8)
        ttk.Button(button_row, text="清除號碼", command=self.clear_inputs).pack(side="left")

        self.summary_var = tk.StringVar(value="尚未開始比對。")
        ttk.Label(form, textvariable=self.summary_var, font=("Microsoft JhengHei", 10)).pack(anchor="w", pady=(10, 0))

        self.summary_frame = ttk.Frame(root)
        self.summary_frame.pack(fill="x", pady=(0, 8))
        self.stat_vars = {"draws": tk.StringVar(value=str(len(DRAWS))), "cost": tk.StringVar(value="NT$0"), "prize": tk.StringVar(value="NT$0"), "net": tk.StringVar(value="NT$0")}
        for key, label in [("draws", "連續簽注期數"), ("cost", "總投注成本"), ("prize", "總中獎獎金"), ("net", "淨收益")]:
            card = ttk.LabelFrame(self.summary_frame, text=label, padding=10)
            ttk.Label(card, textvariable=self.stat_vars[key], style="SummaryValue.TLabel").pack(anchor="w")
            ttk.Label(card, text="依目前玩法與歷史期數更新。", style="SummaryNote.TLabel").pack(anchor="w", pady=(4, 0))
            self.summary_cards.append(card)

        table_box = ttk.LabelFrame(root, text="每期對獎結果", padding=10)
        table_box.pack(fill="both", expand=True)
        columns = ("issue", "date", "numbers", "match", "prize", "detail")
        self.tree = ttk.Treeview(table_box, columns=columns, show="headings", height=20)
        headings = {"issue": "期別", "date": "日期", "numbers": "開出號碼", "match": "對中情況", "prize": "獎金", "detail": "明細"}
        widths = {"issue": 110, "date": 110, "numbers": 220, "match": 120, "prize": 140, "detail": 320}
        for column in columns:
            self.tree.heading(column, text=headings[column])
            self.tree.column(column, width=widths[column], minwidth=96, anchor="w", stretch=True)
        self.tree.pack(side="left", fill="both", expand=True)
        scrollbar = ttk.Scrollbar(table_box, orient="vertical", command=self.tree.yview)
        scrollbar.pack(side="right", fill="y")
        self.tree.configure(yscrollcommand=scrollbar.set)
        self._relayout_summary_cards()

    def _apply_mode(self):
        mode = self.mode_var.get()
        visible = LotteryCalculator.visible_count(mode)
        if mode != "single":
            self.pack_index_var.set(-1)

        self._relayout_entry_holders(visible)

        for index, entry in enumerate(self.entries):
            if index < visible:
                entry.configure(state="normal")
            else:
                entry.configure(state="disabled")
                entry.delete(0, "end")

        for index, button in enumerate(self.pack_buttons):
            if mode == "single" and index < 5:
                button.configure(state="normal", text="取消包牌" if self.pack_index_var.get() == index else "包牌")
            else:
                button.configure(state="disabled", text="包牌")

    def _entry_columns_for_width(self, visible_count):
        width = max(self.winfo_width(), self.winfo_reqwidth())
        if visible_count <= 5:
            return visible_count
        if width >= 1360:
            return min(5, visible_count)
        return min(4, visible_count)

    def _relayout_entry_holders(self, visible_count=None):
        visible_count = visible_count or LotteryCalculator.visible_count(self.mode_var.get())
        columns = self._entry_columns_for_width(visible_count)
        if (
            self._last_entry_columns == columns
            and self._last_visible_count == visible_count
            and all(holder.winfo_manager() for holder in self.entry_holders[:visible_count])
        ):
            return

        self._last_entry_columns = columns
        self._last_visible_count = visible_count
        for column in range(9):
            self.entry_grid.grid_columnconfigure(column, weight=0)

        for index, holder in enumerate(self.entry_holders):
            if index >= visible_count:
                holder.grid_remove()
                continue
            row, column = divmod(index, columns)
            holder.grid(row=row, column=column, padx=8, pady=4, sticky="w")

        for column in range(columns):
            self.entry_grid.grid_columnconfigure(column, weight=0)

    def _summary_columns_for_width(self):
        width = max(self.winfo_width(), self.winfo_reqwidth())
        if width >= 1180:
            return 4
        if width >= 900:
            return 2
        return 1

    def _relayout_summary_cards(self):
        columns = self._summary_columns_for_width()
        if self._last_summary_columns == columns:
            return

        self._last_summary_columns = columns
        for column in range(4):
            self.summary_frame.grid_columnconfigure(column, weight=0)

        for index, card in enumerate(self.summary_cards):
            row, column = divmod(index, columns)
            card.grid(row=row, column=column, padx=4, pady=4, sticky="nsew")

        for column in range(columns):
            self.summary_frame.grid_columnconfigure(column, weight=1, uniform="summary")

    def _on_window_resize(self, event):
        if event.widget is not self:
            return
        self._relayout_entry_holders()
        self._relayout_summary_cards()

    def toggle_pack(self, index):
        if self.mode_var.get() != "single":
            return
        current = self.pack_index_var.get()
        self.pack_index_var.set(-1 if current == index else index)
        self._apply_mode()

    def _entry_values(self):
        return [entry.get().strip() for entry in self.entries]

    def fill_sample(self):
        self.mode_var.set("single")
        self.pack_index_var.set(-1)
        self._apply_mode()
        sample = ["06", "08", "20", "22", "32"]
        for entry in self.entries:
            entry.configure(state="normal")
            entry.delete(0, "end")
        for index, value in enumerate(sample):
            self.entries[index].insert(0, value)
        self._apply_mode()

    def clear_inputs(self):
        for entry in self.entries:
            entry.configure(state="normal")
            entry.delete(0, "end")
        self.pack_index_var.set(-1)
        self.mode_var.set("single")
        self._apply_mode()
        self.summary_var.set("已清除號碼。")
        self._clear_results()

    def _clear_results(self):
        for row in self.tree.get_children():
            self.tree.delete(row)
        self.stat_vars["draws"].set(str(len(DRAWS)))
        self.stat_vars["cost"].set("NT$0")
        self.stat_vars["prize"].set("NT$0")
        self.stat_vars["net"].set("NT$0")

    def analyze(self):
        try:
            selection = LotteryCalculator.build_selection(self.mode_var.get(), self._entry_values(), self.pack_index_var.get() if self.mode_var.get() == "single" and self.pack_index_var.get() >= 0 else None)
        except ValueError as error:
            messagebox.showerror("輸入錯誤", str(error))
            return

        result = LotteryCalculator.analyze(selection)
        self._clear_results()
        for row in result["rows"]:
            self.tree.insert("", "end", values=(row["issue"], row["date"], row["numbers"], row["match"], f"NT${row['prize']:,}", row["detail"]))

        self.stat_vars["draws"].set(str(result["draw_count"]))
        self.stat_vars["cost"].set(f"NT${result['cost_total']:,}")
        self.stat_vars["prize"].set(f"NT${result['prize_total']:,}")
        self.stat_vars["net"].set(f"NT${result['net_total']:,}")
        self.summary_var.set(f"已完成比對，玩法 {selection['label']}，中獎 {result['wins']} 期，成本 NT${result['cost_total']:,}，總獎金 NT${result['prize_total']:,}。")


if __name__ == "__main__":
    App().mainloop()
