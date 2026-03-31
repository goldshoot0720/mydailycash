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

LAYOUT_PROFILES = {
    "laptop": {
        "window_width_ratio": 0.76,
        "window_height_ratio": 0.86,
        "min_window_width": 1080,
        "max_window_width": 1420,
        "min_window_height": 760,
        "max_window_height": 940,
        "minsize": (1024, 700),
        "root_padding": 12,
        "header_padding": (6, 4, 6, 8),
        "form_padding": 12,
        "form_pady": (6, 10),
        "mode_row_pady": (0, 10),
        "entry_holder_padding": (4, 2),
        "entry_padx": 8,
        "entry_pady": 4,
        "entry_width": 6,
        "entry_font": 12,
        "button_row_pady": (10, 0),
        "summary_label_pady": (10, 0),
        "summary_frame_pady": (0, 8),
        "summary_card_padding": 10,
        "summary_card_columns": 4,
        "summary_card_compact_columns": 2,
        "table_padding": 10,
        "header_font": 21,
        "subheader_font": 10,
        "summary_value_font": 16,
        "summary_note_font": 9,
        "single_columns": 5,
        "wide_combo_columns": 5,
        "compact_combo_columns": 4,
        "compact_width_threshold": 1280,
        "table_height": 18,
    },
    "desktop": {
        "window_width_ratio": 0.84,
        "window_height_ratio": 0.88,
        "min_window_width": 1360,
        "max_window_width": 1760,
        "min_window_height": 860,
        "max_window_height": 1100,
        "minsize": (1220, 760),
        "root_padding": 18,
        "header_padding": (8, 6, 8, 10),
        "form_padding": 16,
        "form_pady": (8, 12),
        "mode_row_pady": (0, 14),
        "entry_holder_padding": (6, 4),
        "entry_padx": 12,
        "entry_pady": 6,
        "entry_width": 7,
        "entry_font": 13,
        "button_row_pady": (14, 0),
        "summary_label_pady": (12, 0),
        "summary_frame_pady": (0, 12),
        "summary_card_padding": 14,
        "summary_card_columns": 4,
        "summary_card_compact_columns": 2,
        "table_padding": 12,
        "header_font": 24,
        "subheader_font": 11,
        "summary_value_font": 19,
        "summary_note_font": 10,
        "single_columns": 5,
        "wide_combo_columns": 6,
        "compact_combo_columns": 5,
        "compact_width_threshold": 1500,
        "table_height": 20,
    },
}


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
        self.layout_profile_name = self._detect_layout_profile()
        self.layout_profile = LAYOUT_PROFILES[self.layout_profile_name]
        self.mode_var = tk.StringVar(value="single")
        self.pack_index_var = tk.IntVar(value=-1)
        self.entries = []
        self.pack_buttons = []
        self.entry_holders = []
        self.summary_cards = []
        self.entry_grid = None
        self.summary_frame = None
        self.root_frame = None
        self.header_frame = None
        self.form_frame = None
        self.mode_row = None
        self.button_row = None
        self.summary_label = None
        self.table_box = None
        self.header_label = None
        self.subheader_label = None
        self._last_entry_columns = None
        self._last_summary_columns = None
        self._last_visible_count = None

        self._configure_window()

        self._build_ui()
        self._apply_mode()
        self._clear_results()
        self.bind("<Configure>", self._on_window_resize)

    def _detect_layout_profile(self):
        screen_width = self.winfo_screenwidth()
        screen_height = self.winfo_screenheight()
        if screen_width >= 1800 or (screen_width >= 1600 and screen_height >= 900):
            return "desktop"
        return "laptop"

    def _configure_window(self):
        screen_width = self.winfo_screenwidth()
        screen_height = self.winfo_screenheight()
        profile = self.layout_profile
        width = min(max(int(screen_width * profile["window_width_ratio"]), profile["min_window_width"]), profile["max_window_width"])
        height = min(max(int(screen_height * profile["window_height_ratio"]), profile["min_window_height"]), profile["max_window_height"])
        x = max((screen_width - width) // 2, 0)
        y = max((screen_height - height) // 2, 0)
        self.geometry(f"{width}x{height}+{x}+{y}")
        self.minsize(*profile["minsize"])

    def _build_ui(self):
        style = ttk.Style(self)
        try:
            style.theme_use("clam")
        except tk.TclError:
            pass
        self._apply_style_profile(style)

        self.configure(bg="#f6ead1")
        self.root_frame = ttk.Frame(self, padding=self.layout_profile["root_padding"])
        self.root_frame.pack(fill="both", expand=True)

        self.header_frame = ttk.Frame(self.root_frame, padding=self.layout_profile["header_padding"])
        self.header_frame.pack(fill="x")
        self.header_label = ttk.Label(self.header_frame, text="今彩539 Python GUI 版", style="Header.TLabel")
        self.header_label.pack(anchor="w")
        self.subheader_label = ttk.Label(self.header_frame, text="支援單注、包牌、6連碰、7連碰、8連碰、9連碰收益試算。", style="Subheader.TLabel")
        self.subheader_label.pack(anchor="w", pady=(4, 0))

        self.form_frame = ttk.LabelFrame(self.root_frame, text="輸入設定", padding=self.layout_profile["form_padding"])
        self.form_frame.pack(fill="x", pady=self.layout_profile["form_pady"])

        self.mode_row = ttk.Frame(self.form_frame)
        self.mode_row.pack(fill="x", pady=self.layout_profile["mode_row_pady"])
        for mode, label in [("single", "單注 / 包牌"), ("combo6", "6連碰"), ("combo7", "7連碰"), ("combo8", "8連碰"), ("combo9", "9連碰")]:
            ttk.Radiobutton(self.mode_row, text=label, value=mode, variable=self.mode_var, command=self._apply_mode).pack(side="left", padx=(0, 8))

        self.entry_grid = ttk.Frame(self.form_frame)
        self.entry_grid.pack(anchor="w")
        for index in range(9):
            holder = ttk.Frame(self.entry_grid, padding=self.layout_profile["entry_holder_padding"])
            ttk.Label(holder, text=f"號碼 {index + 1}").pack(anchor="center")
            entry = ttk.Entry(holder, width=self.layout_profile["entry_width"], justify="center", font=("Microsoft JhengHei", self.layout_profile["entry_font"]))
            entry.pack(pady=(2, 3))
            pack_button = ttk.Button(holder, text="包牌", width=8, command=lambda idx=index: self.toggle_pack(idx))
            pack_button.pack()
            self.entries.append(entry)
            self.pack_buttons.append(pack_button)
            self.entry_holders.append(holder)

        self.button_row = ttk.Frame(self.form_frame)
        self.button_row.pack(fill="x", pady=self.layout_profile["button_row_pady"])
        ttk.Button(self.button_row, text="開始比對", command=self.analyze).pack(side="left")
        ttk.Button(self.button_row, text="帶入範例", command=self.fill_sample).pack(side="left", padx=8)
        ttk.Button(self.button_row, text="清除號碼", command=self.clear_inputs).pack(side="left")

        self.summary_var = tk.StringVar(value="尚未開始比對。")
        self.summary_label = ttk.Label(self.form_frame, textvariable=self.summary_var, font=("Microsoft JhengHei", 10))
        self.summary_label.pack(anchor="w", pady=self.layout_profile["summary_label_pady"])

        self.summary_frame = ttk.Frame(self.root_frame)
        self.summary_frame.pack(fill="x", pady=self.layout_profile["summary_frame_pady"])
        self.stat_vars = {"draws": tk.StringVar(value=str(len(DRAWS))), "cost": tk.StringVar(value="NT$0"), "prize": tk.StringVar(value="NT$0"), "net": tk.StringVar(value="NT$0")}
        for key, label in [("draws", "連續簽注期數"), ("cost", "總投注成本"), ("prize", "總中獎獎金"), ("net", "淨收益")]:
            card = ttk.LabelFrame(self.summary_frame, text=label, padding=self.layout_profile["summary_card_padding"])
            ttk.Label(card, textvariable=self.stat_vars[key], style="SummaryValue.TLabel").pack(anchor="w")
            ttk.Label(card, text="依目前玩法與歷史期數更新。", style="SummaryNote.TLabel").pack(anchor="w", pady=(4, 0))
            self.summary_cards.append(card)

        self.table_box = ttk.LabelFrame(self.root_frame, text="每期對獎結果", padding=self.layout_profile["table_padding"])
        self.table_box.pack(fill="both", expand=True)
        columns = ("issue", "date", "numbers", "match", "prize", "detail")
        self.tree = ttk.Treeview(self.table_box, columns=columns, show="headings", height=self.layout_profile["table_height"])
        headings = {"issue": "期別", "date": "日期", "numbers": "開出號碼", "match": "對中情況", "prize": "獎金", "detail": "明細"}
        widths = {"issue": 110, "date": 110, "numbers": 220, "match": 120, "prize": 140, "detail": 320}
        for column in columns:
            self.tree.heading(column, text=headings[column])
            self.tree.column(column, width=widths[column], minwidth=96, anchor="w", stretch=True)
        self.tree.pack(side="left", fill="both", expand=True)
        scrollbar = ttk.Scrollbar(self.table_box, orient="vertical", command=self.tree.yview)
        scrollbar.pack(side="right", fill="y")
        self.tree.configure(yscrollcommand=scrollbar.set)
        self._relayout_summary_cards()

    def _apply_style_profile(self, style):
        profile = self.layout_profile
        style.configure("Header.TLabel", font=("Microsoft JhengHei", profile["header_font"], "bold"))
        style.configure("Subheader.TLabel", font=("Microsoft JhengHei", profile["subheader_font"]))
        style.configure("SummaryValue.TLabel", font=("Microsoft JhengHei", profile["summary_value_font"], "bold"))
        style.configure("SummaryNote.TLabel", font=("Microsoft JhengHei", profile["summary_note_font"]))

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
        profile = self.layout_profile
        width = max(self.winfo_width(), self.winfo_reqwidth())
        if visible_count <= 5:
            return min(profile["single_columns"], visible_count)
        if width >= profile["compact_width_threshold"]:
            return min(profile["wide_combo_columns"], visible_count)
        return min(profile["compact_combo_columns"], visible_count)

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
            holder.grid(row=row, column=column, padx=self.layout_profile["entry_padx"], pady=self.layout_profile["entry_pady"], sticky="w")

        for column in range(columns):
            self.entry_grid.grid_columnconfigure(column, weight=0)

    def _summary_columns_for_width(self):
        profile = self.layout_profile
        width = max(self.winfo_width(), self.winfo_reqwidth())
        if width >= profile["compact_width_threshold"]:
            return profile["summary_card_columns"]
        if width >= 900:
            return profile["summary_card_compact_columns"]
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
