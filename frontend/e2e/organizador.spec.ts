import { expect, test } from "@playwright/test";

test("organizador cria o campeonato, gera a tabela e lança um placar", async ({ page, request }) => {
  await page.goto("/");
  await page.getByRole("link", { name: "Criar meu campeonato" }).click();

  await page.getByLabel("Nome do campeonato").fill("Copa E2E");
  await page.getByRole("button", { name: "Criar campeonato" }).click();
  await expect(page.getByRole("heading", { level: 1, name: "Copa E2E" })).toBeVisible();

  for (const nome of ["Alfa", "Beta", "Gama", "Delta"]) {
    await page.getByLabel("Nome do time").fill(nome);
    await page.getByRole("button", { name: "Inscrever" }).click();
    await expect(page.getByRole("listitem").filter({ hasText: nome })).toBeVisible();
  }

  // a regra de negócio do backend chega até a tela
  await page.getByLabel("Nome do time").fill("alfa");
  await page.getByRole("button", { name: "Inscrever" }).click();
  await expect(page.getByText('Já existe um time chamado "alfa"')).toBeVisible();

  await page.getByRole("button", { name: "Encerrar inscrições e gerar tabela" }).click();
  await expect(page.getByRole("heading", { name: "Classificação" })).toBeVisible({ timeout: 15_000 });
  await expect(page.getByRole("heading", { name: /ª rodada/ })).toHaveCount(3);

  const primeiroJogo = page.locator("form").filter({ has: page.getByRole("button", { name: "Salvar" }) }).first();
  const golsMandante = primeiroJogo.getByRole("spinbutton").first();
  const mandante = (await golsMandante.getAttribute("aria-label"))!.replace("Gols ", "");
  await golsMandante.fill("3");
  await primeiroJogo.getByRole("spinbutton").last().fill("1");
  await primeiroJogo.getByRole("button", { name: "Salvar" }).click();

  await expect(page.getByRole("button", { name: "Corrigir" })).toHaveCount(1);
  const lider = page.getByRole("row").nth(1);
  await expect(lider).toContainText(mandante);
  await expect(lider.getByRole("cell").nth(2)).toHaveText("3");

  // a página pública mostra o mesmo resultado, sem formulários
  await page.getByRole("link", { name: "Página pública" }).click();
  await expect(page.getByRole("row").nth(1)).toContainText(mandante);
  await expect(page.getByText("3 × 1")).toBeVisible();
  await expect(page.getByRole("spinbutton")).toHaveCount(0);

  // preview do link no WhatsApp: o robô não executa JS, então tudo precisa estar no HTML
  const html = await (await request.get(page.url(), { headers: { "User-Agent": "WhatsApp/2.23.20.0 A" } })).text();
  expect(html).toContain('<meta property="og:title" content="Copa E2E"/>');
  const imagem = html.match(/<meta property="og:image" content="([^"]+)"/)?.[1];
  expect(imagem).toBeTruthy();
  const png = await request.get(imagem!.replaceAll("&amp;", "&"));
  expect(png.status()).toBe(200);
  expect(png.headers()["content-type"]).toBe("image/png");
});
