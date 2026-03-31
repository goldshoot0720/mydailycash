import math
import tkinter as tk
from tkinter import messagebox, ttk


PRIZES = {
    0: 0,
    1: 0,
    2: 50,
    3: 300,
    4: 20_000,
    5: 8_000_000,
}

TICKET_PRICE = 50

DRAWS = [
    {"issue": "115000079", "roc_date": "115/03/30", "numbers": ["06", "08", "20", "22", "32"]},
    {"issue": "115000078", "roc_date": "115/03/28", "numbers": ["06", "09", "11", "16", "17"]},
    {"issue": "115000077", "roc_date": "115/03/27", "numbers": ["08", "18", "24", "34", "35"]},
    {"issue": "115000076", "roc_date": "115/03/26", "numbers": ["14", "17", "20", "24", "37"]},
    {"issue": "115000075", "roc_date": "115/03/25", "numbers": ["03", "13", "31", "33", "36"]},
    {"issue": "115000074", "roc_date": "115/03/24", "numbers": ["10", "20", "28", "29", "36"]},
    {"issue": "115000073", "roc_date": "115/03/23", "numbers": ["07", "12", "24", "29", "35"]},
    {"issue": "115000072", "roc_date": "115/03/21", "numbers": ["07", "14", "15", "19", "22"]},
    {"issue": "115000071", "roc_date": "115/03/20", "numbers": ["03", "11", "15", "33", "39"]},
    {"issue": "115000070", "roc_date": "115/03/19", "numbers": ["05", "23", "25", "30", "37"]},
    {"issue": "115000069", "roc_date": "115/03/18", "numbers": ["21", "22", "31", "32", "35"]},
    {"issue": "115000068", "roc_date": "115/03/17", "numbers": ["11", "13", "19", "22", "27"]},
    {"issue": "115000067", "roc_date": "115/03/16", "numbers": ["17", "19", "21", "29", "34"]},
    {"issue": "115000066", "roc_date": "115/03/14", "numbers": ["08", "10", "18", "20", "34"]},
    {"issue": "115000065", "roc_date": "115/03/13", "numbers": ["02", "05", "11", "12", "15"]},
    {"issue": "115000064", "roc_date": "115/03/12", "numbers": ["04", "05", "07", "23", "35"]},
    {"issue": "115000063", "roc_date": "115/03/11", "numbers": ["05", "15", "26", "37", "38"]},
    {"issue": "115000062", "roc_date": "115/03/10", "numbers": ["11", "12", "14", "17", "32"]},
    {"issue": "115000061", "roc_date": "115/03/09", "numbers": ["07", "12", "15", "32", "38"]},
    {"issue": "115000060", "roc_date": "115/03/07", "numbers": ["15", "17", "18", "34", "36"]},
    {"issue": "115000059", "roc_date": "115/03/06", "numbers": ["19", "24", "29", "32", "34"]},
    {"issue": "115000058", "roc_date": "115/03/05", "numbers": ["01", "04", "08", "12", "36"]},
    {"issue": "115000057", "roc_date": "115/03/04", "numbers": ["04", "08", "12", "16", "17"]},
    {"issue": "115000056", "roc_date": "115/03/03", "numbers": ["02", "19", "21", "32", "35"]},
    {"issue": "115000055", "roc_date": "115/03/02", "numbers": ["03", "12", "20", "21", "27"]},
    {"issue": "115000054", "roc_date": "115/03/01", "numbers": ["02", "08", "15", "29", "31"]},
    {"issue": "115000053", "roc_date": "115/02/28", "numbers": ["02", "04", "13", "26", "27"]},
    {"issue": "115000052", "roc_date": "115/02/27", "numbers": ["01", "22", "23", "37", "39"]},
    {"issue": "115000051", "roc_date": "115/02/26", "numbers": ["03", "06", "09", "31", "39"]},
    {"issue": "115000050", "roc_date": "115/02/25", "numbers": ["05", "22", "28", "35", "36"]},
    {"issue": "115000049", "roc_date": "115/02/24", "numbers": ["16", "23", "25", "32", "36"]},
    {"issue": "115000048", "roc_date": "115/02/23", "numbers": ["03", "10", "12", "27", "36"]},
    {"issue": "115000047", "roc_date": "115/02/22", "numbers": ["08", "13", "16", "24", "25"]},
    {"issue": "115000046", "roc_date": "115/02/21", "numbers": ["01", "08", "19", "20", "25"]},
    {"issue": "115000045", "roc_date": "115/02/20", "numbers": ["04", "11", "22", "23", "27"]},
    {"issue": "115000044", "roc_date": "115/02/19", "numbers": ["08", "15", "19", "25", "27"]},
    {"issue": "115000043", "roc_date": "115/02/18", "numbers": ["08", "10", "12", "32", "33"]},
    {"issue": "115000042", "roc_date": "115/02/17", "numbers": ["06", "08", "11", "20", "21"]},
    {"issue": "115000041", "roc_date": "115/02/16", "numbers": ["05", "07", "15", "18", "34"]},
    {"issue": "115000040", "roc_date": "115/02/15", "numbers": ["11", "13", "18", "22", "34"]},
    {"issue": "115000039", "roc_date": "115/02/14", "numbers": ["01", "03", "13", "31", "36"]},
    {"issue": "115000038", "roc_date": "115/02/13", "numbers": ["04", "28", "31", "33", "34"]},
    {"issue": "115000037", "roc_date": "115/02/12", "numbers": ["01", "12", "21", "35", "37"]},
    {"issue": "115000036", "roc_date": "115/02/11", "numbers": ["11", "15", "18", "29", "33"]},
    {"issue": "115000035", "roc_date": "115/02/10", "numbers": ["10", "11", "17", "22", "36"]},
    {"issue": "115000034", "roc_date": "115/02/09", "numbers": ["16", "21", "25", "31", "35"]},
    {"issue": "115000033", "roc_date": "115/02/07", "numbers": ["03", "08", "22", "27", "32"]},
    {"issue": "115000032", "roc_date": "115/02/06", "numbers": ["01", "06", "29", "32", "34"]},
    {"issue": "115000031", "roc_date": "115/02/05", "numbers": ["08", "09", "13", "32", "35"]},
    {"issue": "115000030", "roc_date": "115/02/04", "numbers": ["08", "17", "22", "27", "28"]},
    {"issue": "115000029", "roc_date": "115/02/03", "numbers": ["03", "05", "11", "15", "23"]},
    {"issue": "115000028", "roc_date": "115/02/02", "numbers": ["06", "08", "31", "37", "38"]},
    {"issue": "115000027", "roc_date": "115/01/31", "numbers": ["05", "12", "16", "21", "32"]},
    {"issue": "115000026", "roc_date": "115/01/30", "numbers": ["16", "17", "29", "30", "36"]},
    {"issue": "115000025", "roc_date": "115/01/29", "numbers": ["06", "11", "28", "36", "37"]},
    {"issue": "115000024", "roc_date": "115/01/28", "numbers": ["10", "11", "23", "24", "29"]},
    {"issue": "115000023", "roc_date": "115/01/27", "numbers": ["05", "17", "18", "23", "32"]},
    {"issue": "115000022", "roc_date": "115/01/26", "numbers": ["06", "15", "23", "26", "30"]},
    {"issue": "115000021", "roc_date": "115/01/24", "numbers": ["06", "07", "15", "35", "37"]},
    {"issue": "115000020", "roc_date": "115/01/23", "numbers": ["03", "11", "12", "21", "31"]},
    {"issue": "115000019", "roc_date": "115/01/22", "numbers": ["03", "06", "11", "30", "34"]},
    {"issue": "115000018", "roc_date": "115/01/21", "numbers": ["04", "15", "23", "27", "38"]},
    {"issue": "115000017", "roc_date": "115/01/20", "numbers": ["16", "19", "23", "25", "34"]},
    {"issue": "115000016", "roc_date": "115/01/19", "numbers": ["12", "16", "23", "24", "29"]},
    {"issue": "115000015", "roc_date": "115/01/17", "numbers": ["02", "10", "11", "24", "37"]},
    {"issue": "115000014", "roc_date": "115/01/16", "numbers": ["18", "19", "22", "27", "29"]},
    {"issue": "115000013", "roc_date": "115/01/15", "numbers": ["01", "02", "03", "19", "36"]},
    {"issue": "115000012", "roc_date": "115/01/14", "numbers": ["01", "02", "16", "33", "35"]},
    {"issue": "115000011", "roc_date": "115/01/13", "numbers": ["06", "16", "17", "19", "31"]},
    {"issue": "115000010", "roc_date": "115/01/12", "numbers": ["03", "13", "18", "24", "30"]},
    {"issue": "115000009", "roc_date": "115/01/10", "numbers": ["11", "25", "26", "34", "38"]},
    {"issue": "115000008", "roc_date": "115/01/09", "numbers": ["01", "12", "14", "22", "34"]},
    {"issue": "115000007", "roc_date": "115/01/08", "numbers": ["03", "08", "10", "21", "30"]},
    {"issue": "115000006", "roc_date": "115/01/07", "numbers": ["05", "10", "14", "15", "28"]},
    {"issue": "115000005", "roc_date": "115/01/06", "numbers": ["01", "02", "06", "11", "33"]},
    {"issue": "115000004", "roc_date": "115/01/05", "numbers": ["10", "16", "18", "34", "39"]},
    {"issue": "115000003", "roc_date": "115/01/03", "numbers": ["22", "23", "31", "32", "38"]},
    {"issue": "115000002", "roc_date": "115/01/02", "numbers": ["17", "18", "25", "36", "39"]},
    {"issue": "115000001", "roc_date": "115/01/01", "numbers": ["15", "16", "18", "29", "36"]},
]


class LotteryCalculator:
    @staticmethod
    def pad(number):
        return str(number).zfill(2)

    @staticmethod
    def combo(n, r):
        return math.comb(n, r)

    @classmethod
    def build_selection(cls, mode, values, pack_index=None):
        visible = cls.visible_count(mode)
        active_values = values[:visible]

        if mode.startswith("combo"):
            numbers = cls.validate_numbers(active_values, visible)
            return {
                "mode": mode,
                "numbers": numbers,
                "combo_size": visible,
                "label": f"{visible}連碰 {' '.join(numbers)}",
            }

        if pack_index is None:
            numbers = cls.validate_numbers(active_values[:5], 5)
            return {"mode": "single", "numbers": numbers, "label": " ".join(numbers)}

        fixed = [value for index, value in enumerate(active_values[:5]) if index != pack_index]
        numbers = cls.validate_numbers(fixed, 4)
        return {
            "mode": "pack",
            "numbers": numbers,
            "pack_index": pack_index,
            "label": f"{' '.join(numbers)} + 包牌",
        }

    @classmethod
    def validate_numbers(cls, values, count):
        if any(value == "" for value in values) or len(values) != count:
            raise ValueError(f"請輸入 {count} 個號碼。")
        numbers = [int(value) for value in values]
        if any(number < 1 or number > 39 for number in numbers):
            raise ValueError("號碼必須在 1 到 39 之間。")
        if len(set(numbers)) != count:
            raise ValueError("號碼不可重複。")
        return [cls.pad(number) for number in sorted(numbers)]

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

    @classmethod
    def analyze(cls, selection):
        rows = []
        selected = set(selection["numbers"])

        for draw in DRAWS:
            draw_numbers = draw["numbers"]

            if selection["mode"] == "pack":
                fixed_hits = [number for number in draw_numbers if number in selected]
                pack_covered = [number for number in draw_numbers if number not in selected]
                fixed_count = len(fixed_hits)
                upgraded = fixed_count + 1
                upgraded_tickets = len(pack_covered)
                base_tickets = 35 - upgraded_tickets
                prize = upgraded_tickets * PRIZES[upgraded] + base_tickets * PRIZES[fixed_count]
                rows.append(
                    {
                        "issue": draw["issue"],
                        "date": draw["roc_date"],
                        "numbers": " ".join(draw_numbers),
                        "match": f"固定中 {fixed_count} 個",
                        "prize": prize,
                        "detail": "包牌命中: " + (" ".join(pack_covered) if pack_covered else "-"),
                    }
                )
                continue

            if selection["mode"].startswith("combo"):
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
                        "date": draw["roc_date"],
                        "numbers": " ".join(draw_numbers),
                        "match": f"選中 {hit_count} 個",
                        "prize": prize,
                        "detail": "命中號碼: " + (" ".join(hits) if hits else "-"),
                    }
                )
                continue

            hits = [number for number in draw_numbers if number in selected]
            match_count = len(hits)
            rows.append(
                {
                    "issue": draw["issue"],
                    "date": draw["roc_date"],
                    "numbers": " ".join(draw_numbers),
                    "match": f"對中 {match_count} 個",
                    "prize": PRIZES[match_count],
                    "detail": "命中號碼: " + (" ".join(hits) if hits else "-"),
                }
            )

        if selection["mode"] == "pack":
            cost_per_draw = TICKET_PRICE * 35
        elif selection["mode"].startswith("combo"):
            cost_per_draw = cls.combo(selection["combo_size"], 5) * TICKET_PRICE
        else:
            cost_per_draw = TICKET_PRICE

        prize_total = sum(row["prize"] for row in rows)
        cost_total = len(rows) * cost_per_draw
        return {
            "rows": rows,
            "draw_count": len(rows),
            "wins": sum(1 for row in rows if row["prize"] > 0),
            "cost_per_draw": cost_per_draw,
            "cost_total": cost_total,
            "prize_total": prize_total,
            "net_total": prize_total - cost_total,
        }


class App(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("今彩539 Python GUI 版")
        self.geometry("1420x860")
        self.minsize(1240, 760)

        self.mode_var = tk.StringVar(value="single")
        self.pack_index_var = tk.IntVar(value=-1)
        self.entries = []
        self.pack_radios = []

        self._build_ui()
        self._apply_mode()

    def _build_ui(self):
        self.configure(bg="#f7ecd4")
        container = ttk.Frame(self, padding=16)
        container.pack(fill="both", expand=True)

        top = ttk.Frame(container, padding=16)
        top.pack(fill="x")

        ttk.Label(top, text="今彩539 Python GUI 版", font=("Microsoft JhengHei", 22, "bold")).pack(anchor="w")
        ttk.Label(
            top,
            text="支援單注、包牌、6連碰、7連碰、8連碰、9連碰收益試算。",
            font=("Microsoft JhengHei", 11),
        ).pack(anchor="w", pady=(8, 0))

        form = ttk.LabelFrame(container, text="輸入設定", padding=16)
        form.pack(fill="x", pady=(8, 12))

        mode_row = ttk.Frame(form)
        mode_row.pack(fill="x", pady=(0, 14))
        for mode, label in [
            ("single", "單注 / 包牌"),
            ("combo6", "6連碰"),
            ("combo7", "7連碰"),
            ("combo8", "8連碰"),
            ("combo9", "9連碰"),
        ]:
            ttk.Radiobutton(mode_row, text=label, value=mode, variable=self.mode_var, command=self._apply_mode).pack(side="left", padx=(0, 10))

        entry_grid = ttk.Frame(form)
        entry_grid.pack(fill="x")
        for index in range(9):
            col = index % 9
            holder = ttk.Frame(entry_grid)
            holder.grid(row=0, column=col, padx=4, pady=4, sticky="nsew")
            ttk.Label(holder, text=f"號碼 {index + 1}").pack(anchor="center")
            entry = ttk.Entry(holder, width=6, justify="center", font=("Microsoft JhengHei", 12))
            entry.pack(pady=(4, 4))
            pack_radio = ttk.Radiobutton(holder, text="包牌", value=index, variable=self.pack_index_var)
            pack_radio.pack()
            self.entries.append(entry)
            self.pack_radios.append(pack_radio)

        button_row = ttk.Frame(form)
        button_row.pack(fill="x", pady=(16, 0))
        ttk.Button(button_row, text="開始比對", command=self.analyze).pack(side="left")
        ttk.Button(button_row, text="帶入範例", command=self.fill_sample).pack(side="left", padx=8)
        ttk.Button(button_row, text="清除號碼", command=self.clear_inputs).pack(side="left")

        self.summary_var = tk.StringVar(value="尚未開始比對。")
        ttk.Label(form, textvariable=self.summary_var, font=("Microsoft JhengHei", 10)).pack(anchor="w", pady=(14, 0))

        summary = ttk.Frame(container)
        summary.pack(fill="x", pady=(0, 12))
        self.stat_vars = {
            "draws": tk.StringVar(value="79"),
            "cost": tk.StringVar(value="NT$0"),
            "prize": tk.StringVar(value="NT$0"),
            "net": tk.StringVar(value="NT$0"),
        }

        for key, label in [("draws", "連續簽注期數"), ("cost", "總投注成本"), ("prize", "總中獎獎金"), ("net", "淨收益")]:
            card = ttk.LabelFrame(summary, text=label, padding=14)
            card.pack(side="left", fill="both", expand=True, padx=4)
            ttk.Label(card, textvariable=self.stat_vars[key], font=("Microsoft JhengHei", 20, "bold")).pack(anchor="w")

        table_box = ttk.LabelFrame(container, text="每期對獎結果", padding=12)
        table_box.pack(fill="both", expand=True)

        columns = ("issue", "date", "numbers", "match", "prize", "detail")
        self.tree = ttk.Treeview(table_box, columns=columns, show="headings", height=20)
        headings = {
            "issue": "期別",
            "date": "日期",
            "numbers": "開出號碼",
            "match": "對中情況",
            "prize": "獎金",
            "detail": "明細",
        }
        widths = {
            "issue": 100,
            "date": 95,
            "numbers": 200,
            "match": 110,
            "prize": 120,
            "detail": 280,
        }
        for column in columns:
            self.tree.heading(column, text=headings[column])
            self.tree.column(column, width=widths[column], anchor="w")
        self.tree.pack(side="left", fill="both", expand=True)

        scrollbar = ttk.Scrollbar(table_box, orient="vertical", command=self.tree.yview)
        scrollbar.pack(side="right", fill="y")
        self.tree.configure(yscrollcommand=scrollbar.set)

    def _apply_mode(self):
        mode = self.mode_var.get()
        visible = LotteryCalculator.visible_count(mode)

        for index, entry in enumerate(self.entries):
            state = "normal" if index < visible else "disabled"
            if state == "disabled":
                entry.delete(0, "end")
            entry.configure(state=state)

        for index, radio in enumerate(self.pack_radios):
            if mode == "single" and index < 5:
                radio.configure(state="normal")
            else:
                radio.configure(state="disabled")
        if mode != "single":
            self.pack_index_var.set(-1)

    def _entry_values(self):
        values = []
        for entry in self.entries:
            value = entry.get().strip()
            values.append(value)
        return values

    def fill_sample(self):
        self.mode_var.set("single")
        self._apply_mode()
        sample = ["06", "08", "20", "22", "32"]
        for entry in self.entries:
            entry.configure(state="normal")
            entry.delete(0, "end")
        for index, value in enumerate(sample):
            self.entries[index].insert(0, value)
        self.pack_index_var.set(-1)

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
            selection = LotteryCalculator.build_selection(
                self.mode_var.get(),
                self._entry_values(),
                self.pack_index_var.get() if self.mode_var.get() == "single" and self.pack_index_var.get() >= 0 else None,
            )
        except ValueError as error:
            messagebox.showerror("輸入錯誤", str(error))
            return

        result = LotteryCalculator.analyze(selection)
        self._clear_results()
        for row in result["rows"]:
            self.tree.insert(
                "",
                "end",
                values=(
                    row["issue"],
                    row["date"],
                    row["numbers"],
                    row["match"],
                    f"NT${row['prize']:,}",
                    row["detail"],
                ),
            )

        self.stat_vars["draws"].set(str(result["draw_count"]))
        self.stat_vars["cost"].set(f"NT${result['cost_total']:,}")
        self.stat_vars["prize"].set(f"NT${result['prize_total']:,}")
        self.stat_vars["net"].set(f"NT${result['net_total']:,}")
        self.summary_var.set(
            f"已完成比對，玩法 {selection['label']}，中獎 {result['wins']} 期，成本 NT${result['cost_total']:,}，總獎金 NT${result['prize_total']:,}。"
        )


if __name__ == "__main__":
    App().mainloop()
