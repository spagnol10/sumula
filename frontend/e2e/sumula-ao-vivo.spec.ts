import { expect, test } from "@playwright/test";

test("gol lançado na súmula aparece na página pública sem recarregar", async ({ page, browser }) => {
  await page.goto("/campeonatos/novo");
  await page.getByLabel("Nome do campeonato").fill("Copa Ao Vivo");
  await page.getByRole("button", { name: "Criar campeonato" }).click();
  for (const nome of ["Vila", "Morro"]) {
    await page.getByLabel("Nome do time").fill(nome);
    await page.getByRole("button", { name: "Inscrever" }).click();
    await expect(page.getByRole("listitem").filter({ hasText: nome })).toBeVisible();
  }

  const elencoDaVila = page.locator("details", { has: page.locator("summary", { hasText: "Vila" }) });
  await elencoDaVila.locator("summary").click();
  await elencoDaVila.getByLabel("Nome do jogador").fill("Zé");
  await elencoDaVila.getByLabel("Número da camisa").fill("9");
  await elencoDaVila.getByRole("button", { name: "Adicionar" }).click();
  await expect(elencoDaVila.getByText("9 · Zé")).toBeVisible();

  await page.getByRole("button", { name: "Encerrar inscrições e gerar tabela" }).click();
  await expect(page.getByRole("heading", { name: "Classificação" })).toBeVisible({ timeout: 15_000 });

  // torcedor acompanhando de outro aparelho
  const urlPublica = await page.getByRole("link", { name: "Página pública" }).getAttribute("href");
  const torcida = await browser.newContext();
  const publica = await torcida.newPage();
  await publica.goto(urlPublica!);
  await expect(publica.getByTitle("Atualizando em tempo real")).toBeVisible();

  await page.getByRole("link", { name: "Abrir súmula" }).click();
  await page.getByRole("button", { name: "Iniciar partida" }).click();
  const vila = page.getByRole("form", { name: "Lançamentos do Vila" });
  await vila.getByLabel("Jogador do Vila").selectOption({ label: "9 · Zé" });
  await vila.getByLabel("Minuto").fill("10");
  await vila.getByRole("button", { name: "⚽ Gol" }).click();
  await expect(page.getByText(/1 × 0|0 × 1/)).toBeVisible();

  // a página pública atualiza sozinha via SSE
  await expect(publica.getByText(/1 × 0|0 × 1/)).toBeVisible({ timeout: 10_000 });
  await expect(publica.getByText("⚽ Zé 10'")).toBeVisible();
  await expect(publica.getByRole("heading", { name: "Artilharia" })).toBeVisible();

  // regra de negócio: cartão exige jogador
  await page.getByRole("form", { name: "Lançamentos do Morro" }).getByRole("button", { name: "🟥 Vermelho" }).click();
  await expect(page.getByText("Informe o jogador que recebeu o cartão")).toBeVisible();

  await page.getByRole("button", { name: "Encerrar partida" }).click();
  await expect(publica.getByText("Encerrado")).toBeVisible({ timeout: 10_000 });
  await torcida.close();
});
