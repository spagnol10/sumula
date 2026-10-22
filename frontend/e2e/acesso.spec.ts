import { expect, test } from "@playwright/test";

test.use({ permissions: ["clipboard-read", "clipboard-write"] });

test("só quem tem a chave administra; a página pública é somente leitura", async ({ page, browser }) => {
  await page.goto("/campeonatos/novo");
  await page.getByLabel("Nome do campeonato").fill("Copa Acesso");
  await page.getByRole("button", { name: "Criar campeonato" }).click();
  await expect(page.getByRole("button", { name: "Inscrever" })).toBeVisible();
  const urlDoOrganizador = page.url();

  await page.getByRole("button", { name: "Copiar meu link de acesso" }).click();
  const linkDeAcesso = await page.evaluate(() => navigator.clipboard.readText());
  expect(linkDeAcesso).toContain("/acesso?chave=");

  // outro aparelho, sem cookie: não administra
  const visitante = await browser.newContext();
  const outra = await visitante.newPage();
  await outra.goto(urlDoOrganizador);
  await expect(outra.getByText("Esta é a área do organizador")).toBeVisible();
  await expect(outra.getByRole("button", { name: "Inscrever" })).toHaveCount(0);

  // com o link de acesso, passa a administrar e a chave sai da barra de endereços
  await outra.goto(linkDeAcesso);
  await expect(outra.getByRole("button", { name: "Inscrever" })).toBeVisible();
  expect(outra.url()).not.toContain("chave=");
  await visitante.close();

  // página pública: só leitura
  await page.getByRole("link", { name: "Página pública" }).click();
  await expect(page).toHaveURL(/\/c\/copa-acesso-/);
  await expect(page.getByText("Inscrições abertas")).toBeVisible();
  await expect(page.getByRole("button")).toHaveCount(0);
});
